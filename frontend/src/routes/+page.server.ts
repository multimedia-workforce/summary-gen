import type { PageServerLoad } from "./$types";

const HEARTBEAT_INTERVAL = Number(process.env.GRPC_HEARTBEAT_INTERVAL ?? 10000);

export const load: PageServerLoad = async ({ params }) => {
    return {
        heartbeatInterval: HEARTBEAT_INTERVAL,
    };
};