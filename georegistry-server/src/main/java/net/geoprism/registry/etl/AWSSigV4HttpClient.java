package net.geoprism.registry.etl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.regions.Region;

public class AWSSigV4HttpClient extends HttpClient
{
  private final HttpClient delegate;
  private final AwsCredentialsProvider credentialsProvider;
  private final Region region;

  private final AwsV4HttpSigner signer = AwsV4HttpSigner.create();

  public AWSSigV4HttpClient(
      Region region,
      AwsCredentialsProvider credentialsProvider)
  {
    this.delegate = HttpClient.newBuilder().build();
    this.region = region;
    this.credentialsProvider = credentialsProvider;
  }

  @Override
  public <T> HttpResponse<T> send(
      HttpRequest request,
      HttpResponse.BodyHandler<T> responseBodyHandler)
      throws IOException, InterruptedException
  {
    HttpRequest signedRequest = sign(request);

    return delegate.send(signedRequest, responseBodyHandler);
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request,
      HttpResponse.BodyHandler<T> responseBodyHandler)
  {
    try
    {
      HttpRequest signedRequest = sign(request);

      return delegate.sendAsync(signedRequest, responseBodyHandler);
    }
    catch (Exception e)
    {
      return CompletableFuture.failedFuture(e);
    }
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request,
      HttpResponse.BodyHandler<T> responseBodyHandler,
      HttpResponse.PushPromiseHandler<T> pushPromiseHandler)
  {
    try
    {
      HttpRequest signedRequest = sign(request);

      return delegate.sendAsync(
          signedRequest,
          responseBodyHandler,
          pushPromiseHandler);
    }
    catch (Exception e)
    {
      return CompletableFuture.failedFuture(e);
    }
  }

  private HttpRequest sign(HttpRequest request) throws IOException, InterruptedException
  {
    byte[] body = readBody(request);

    SdkHttpRequest.Builder sdkRequestBuilder = SdkHttpRequest.builder()
        .uri(request.uri())
        .method(SdkHttpMethod.fromValue(request.method()));

    request.headers().map().forEach((name, values) -> {
      values.forEach(value -> sdkRequestBuilder.appendHeader(name, value));
    });

    SdkHttpRequest sdkRequest = sdkRequestBuilder.build();

    AwsCredentials credentials = credentialsProvider.resolveCredentials();

    ContentStreamProvider payload =
        body.length == 0
            ? null
            : ContentStreamProvider.fromByteArray(body);

    SignedRequest signed;

    if (payload != null)
    {
      signed = signer.sign(r -> r
          .identity(credentials)
          .request(sdkRequest)
          .payload(payload)
          .putProperty(
              AwsV4HttpSigner.SERVICE_SIGNING_NAME,
              "neptune-db")
          .putProperty(
              AwsV4HttpSigner.REGION_NAME,
              region.id()));
    }
    else
    {
      signed = signer.sign(r -> r
          .identity(credentials)
          .request(sdkRequest)
          .putProperty(
              AwsV4HttpSigner.SERVICE_SIGNING_NAME,
              "neptune-db")
          .putProperty(
              AwsV4HttpSigner.REGION_NAME,
              region.id()));
    }

    HttpRequest.Builder javaRequest = HttpRequest.newBuilder()
        .uri(signed.request().getUri());

    signed.request().headers().forEach((name, values) -> {
      if (!"host".equalsIgnoreCase(name) &&
          !"content-length".equalsIgnoreCase(name))
      {
        values.forEach(value -> javaRequest.header(name, value));
      }
    });

    HttpRequest.BodyPublisher bodyPublisher =
        body.length == 0
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofByteArray(body);

    javaRequest.method(request.method(), bodyPublisher);

    return javaRequest.build();
  }

  private byte[] readBody(HttpRequest request)
      throws InterruptedException
  {
    Optional<HttpRequest.BodyPublisher> optional =
        request.bodyPublisher();

    if (optional.isEmpty())
    {
      return new byte[0];
    }

    HttpRequest.BodyPublisher publisher = optional.get();

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    CompletableFuture<Void> done = new CompletableFuture<>();

    publisher.subscribe(new Flow.Subscriber<ByteBuffer>()
    {
      private Flow.Subscription subscription;

      @Override
      public void onSubscribe(Flow.Subscription subscription)
      {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(ByteBuffer item)
      {
        byte[] bytes = new byte[item.remaining()];
        item.get(bytes);
        output.write(bytes, 0, bytes.length);
      }

      @Override
      public void onError(Throwable throwable)
      {
        done.completeExceptionally(throwable);
      }

      @Override
      public void onComplete()
      {
        done.complete(null);
      }
    });

    try
    {
      done.join();
    }
    catch (CompletionException e)
    {
      Throwable cause = e.getCause();

      if (cause instanceof RuntimeException runtimeException)
      {
        throw runtimeException;
      }

      throw e;
    }

    return output.toByteArray();
  }

  @Override
  public Optional<CookieHandler> cookieHandler()
  {
    return delegate.cookieHandler();
  }

  @Override
  public Optional<Duration> connectTimeout()
  {
    return delegate.connectTimeout();
  }

  @Override
  public Redirect followRedirects()
  {
    return delegate.followRedirects();
  }

  @Override
  public Optional<ProxySelector> proxy()
  {
    return delegate.proxy();
  }

  @Override
  public SSLContext sslContext()
  {
    return delegate.sslContext();
  }

  @Override
  public SSLParameters sslParameters()
  {
    return delegate.sslParameters();
  }

  @Override
  public Optional<Authenticator> authenticator()
  {
    return delegate.authenticator();
  }

  @Override
  public Version version()
  {
    return delegate.version();
  }

  @Override
  public Optional<Executor> executor()
  {
    return delegate.executor();
  }
}
