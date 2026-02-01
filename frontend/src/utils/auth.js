export function handleUnauthorized(err, options = {}) {
  const {
    requireLogin = false,
    onUnauthorized = null,
    redirectTo = '/login',
    message = '로그인이 필요합니다.',
    router = null,
  } = options;

  const status = err?.response?.status;
  if (status !== 401) return false;

  if (typeof onUnauthorized === 'function') {
    onUnauthorized(err);
  }

  if (requireLogin) {
    if (message) {
      window.alert(message);
    }

    if (router?.push) {
      router.push({ path: redirectTo });
    } else {
      window.location.replace(redirectTo);
    }
  }

  return true;
}
