import type {Handle} from '@sveltejs/kit';
import {redirect} from '@sveltejs/kit';

const PERSISTENCE_URL = process.env.PERSISTENCE_URL ?? 'http://localhost:8081';

export const handle: Handle = async ({event, resolve}) => {
    const jwt = event.cookies.get('jwt');

    if (jwt) {
        try {
            const res = await fetch(`${PERSISTENCE_URL}/auth/me`, {
                headers: {
                    Authorization: `Bearer ${jwt}`
                }
            });

            if (res.ok) {
                const user = await res.json();
                event.locals.user = user;
                event.locals.jwt = jwt; // Store the JWT so it can be reused
            } else {
                event.locals.user = null;
                event.locals.jwt = null;
            }
        } catch (e) {
            console.error('JWT validation failed:', e);
            event.locals.user = null;
            event.locals.jwt = null;
        }
    } else {
        event.locals.user = null;
        event.locals.jwt = null;
    }

    const path = event.url.pathname;

    // Exclude public routes from protection
    const isPublic = path.startsWith('/auth') || path.startsWith('/api');

    if (!isPublic && !event.locals.user) {
        throw redirect(302, '/auth');
    }

    return resolve(event);
};
