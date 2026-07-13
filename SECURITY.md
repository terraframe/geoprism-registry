# Security Policy

While the GeoPrism Registry community and the primary stewards (TerraFrame) cannot guarantee immediate responses or fixes, we take security reports seriously and appreciate responsible disclosure of potential vulnerabilities.

## Supported Versions

Security updates are provided on a best-effort basis through a combination of automated monitoring, developer process, and manual checks. The current release is always the most secure as previous releases are rarely updated with security fixes.

| Version               | Supported                 |
| --------------------- | ------------------------- |
| Latest stable release | ✅ Best effort             |
| Older releases        | ❌ Not routinely supported |
| Development branches  | ❌ No security guarantees  |

We encourage all users to upgrade to the latest stable release whenever possible.

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, report potential vulnerabilities by contacting the project maintainers privately.

Include as much information as possible, such as:

* A description of the vulnerability
* Steps to reproduce the issue
* A proof of concept, if available
* The affected version(s)
* Any suggested mitigations or fixes

If you are unsure whether something is a security issue, we encourage you to report it.

## What to Expect

Because GeoPrism Registry is maintained by a small team, response times may vary depending on maintainer availability.

Our goals are to:

* Acknowledge receipt of a report within **five business days**, when possible.
* Confirm whether the issue appears to be a legitimate security vulnerability.
* Work with the reporter to understand the issue and develop a fix if warranted.
* Coordinate public disclosure after a fix or mitigation is available whenever practical.

These are goals rather than guarantees.

## Responsible Disclosure

We ask that security researchers and users avoid publicly disclosing vulnerabilities until we have had a reasonable opportunity to investigate and, where possible, prepare a fix or mitigation.

We are committed to working collaboratively with reporters throughout the disclosure process.

## Scope

This policy applies to the GeoPrism Registry source code and officially maintained releases.

Issues involving third-party software, deployment environments, operating systems, cloud infrastructure, or dependencies may need to be reported to the appropriate upstream projects or vendors.

## Security Best Practices

Users deploying GeoPrism Registry should:

* Keep GeoPrism Registry up to date with the latest stable release.
* Apply operating system and dependency security updates.
* Use HTTPS for production deployments.
* Restrict administrative access to trusted users.
* Follow your organization's security policies for authentication, authorization, backups, and infrastructure management.

## Acknowledgements

We appreciate the efforts of security researchers and community members who responsibly report vulnerabilities. With the reporter's permission, we are happy to acknowledge contributions in release notes or project documentation after a vulnerability has been addressed.
