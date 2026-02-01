import axios from "axios";
import { useAccountStore } from "@/stores/account";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
axios.defaults.baseURL = API_BASE_URL;

const instance = axios.create({
    withCredentials: true, // refresh token cookie attach
    baseURL: API_BASE_URL,
});

// response interceptor
instance.interceptors.response.use(
    (res) => res,
  async (err) => {
    const accountStore = useAccountStore();
    const accessToken =
      accountStore.accessToken || localStorage.getItem('accessToken');

        if (err.response?.status === 401) {
            const config = err.config || {};

            const isAuthBypass =
                config.skipAuth ||
                config.url?.includes('/api/login') ||
                config.url?.includes('/api/refresh');

      if (isAuthBypass) return Promise.reject(err);

      if (!accessToken) {
        return Promise.reject(err);
      }

            if (config._retry) return Promise.reject(err);
            config._retry = true;

            try {
                const refreshRes = await axios.post('/api/refresh', null, {
                    withCredentials: true,
                });

                const accessToken = refreshRes.data?.accessToken ?? refreshRes.data;
                if (!accessToken || typeof accessToken !== 'string') {
                    throw new Error('invalid refresh token response');
                }

                accountStore.setAccessToken(accessToken);
                localStorage.setItem('accessToken', accessToken);

                config.headers = config.headers || {};
                config.headers.Authorization = `Bearer ${accessToken}`;

                return instance(config);
            } catch (refreshError) {
                const status = refreshError?.response?.status;
                if (status === 401 || status === 403) {
                    accountStore.clearAccount?.();
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('member');
                }
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(err);
    }
);

const generateConfig = (options = {}) => {
    const { skipAuth, ...rest } = options;

    if (skipAuth) {
        return rest.headers ? { ...rest, headers: { ...rest.headers } } : rest;
    }

    const accountStore = useAccountStore();
    const token = accountStore.accessToken || localStorage.getItem('accessToken');
    const headers = rest.headers ? { ...rest.headers } : {};

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return Object.keys(headers).length ? { ...rest, headers } : rest;
}

export default {
    get(url, params, options = {}) {
        const config = generateConfig(options);
        config.params = params;
        return instance.get(url, config);
    },
    post(url, params, options = {}) {
        return instance.post(url, params, generateConfig(options));
    },
    put(url, params, options = {}) {
        return instance.put(url, params, generateConfig(options));
    },
    patch(url, params, options = {}) {
        return instance.patch(url, params, generateConfig(options));
    },
    delete(url, options = {}) {
        return instance.delete(url, generateConfig(options));
    },
};
