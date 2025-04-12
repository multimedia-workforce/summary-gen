import { json } from '@sveltejs/kit';

const PERSISTENCE_URL = process.env.PERSISTENCE_URL ?? 'http://localhost:8081';

export async function POST({ request, cookies }) {
    const credentials = await request.json();

    const res = await fetch(`${PERSISTENCE_URL}/auth/register`, {
        method: 'POST',
        body: JSON.stringify(credentials),
        headers: { 'Content-Type': 'application/json' }
    });

    if (!res.ok) {
        return json({ message: 'Registration failed' }, { status: res.status });
    }

    const token = await res.text();

    cookies.set('jwt', token, {
        httpOnly: true,
        path: '/',
        secure: false, // change to true in production
        sameSite: 'strict',
        maxAge: 60 * 60
    });

    return json({ success: true });
}
