window.adminFetch = function (url, options = {}) {
    const target = new URL(url, window.location.href);

    if (target.origin !== window.location.origin) {
        throw new Error('Only same-origin requests are allowed');
    }

    const method = (options.method || 'GET').toUpperCase();
    const headers = new Headers(options.headers);

    if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;

        if (!token || !header) {
            throw new Error('CSRF token is missing');
        }

        headers.set(header, token);
    }

    return fetch(url, {
        ...options,
        headers
    });
};