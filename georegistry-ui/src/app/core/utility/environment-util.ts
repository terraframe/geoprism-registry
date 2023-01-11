import { environment } from "src/environments/environment";

export default class EnvironmentUtil {

    static getApiUrl(): string {
        let context = environment.apiUrl;

        if (context == '.') {
            context = "";
        }

        return context;

    }

}
