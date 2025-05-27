// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
    namespace App {
        interface Locals {
            user: {
                id: string;
                username: string;
            } | null;
            jwt: string | null;
        }
    }
}

export {};
