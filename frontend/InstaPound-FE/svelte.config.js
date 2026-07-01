import adapter from '@sveltejs/adapter-auto';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		// adapter-auto only supports some environments, see https://svelte.dev/docs/kit/adapter-auto for a list.
		// If your environment is not supported, or you settled on a specific environment, switch out the adapter.
		// See https://svelte.dev/docs/kit/adapters for more information about adapters.
		adapter: adapter(),
        csp: {
            mode: 'hash',
            directives: {
                'default-src': ['self'],
                'script-src': ['self'],
                'style-src': ['self'],
                'img-src': ['self', 'data:', 'blob:'],
                'font-src': ['self'],
                'connect-src': ['self', 'http://localhost:8080'],
                'frame-ancestors': ['none'],
                'form-action': ['self']
            }
        }
	}
};

export default config;
