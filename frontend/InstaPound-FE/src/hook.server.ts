import type { Handle } from '@sveltejs/kit';

export const handle: Handle = async ({ event, resolve }) => {
    const response = await resolve(event);

    response.headers.set(
        'Content-Security-Policy',
        "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'" +
        " data: blob:; font-src 'self'; connect-src 'self' http://localhost:8080; frame-ancestors 'none';" +
        " form-action 'self';"
    );

    response.headers.set('X-Frame-Options', 'DENY');

    response.headers.set('X-Content-Type-Options', 'nosniff');

    response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');

    return response;
}