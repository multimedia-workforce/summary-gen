// src/routes/+layout.server.ts
export async function load({ locals }) {
    return {
        user: locals.user
    };
}
