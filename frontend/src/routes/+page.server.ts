import type { PageServerLoad } from "./$types";

const HEARTBEAT_INTERVAL = Number(process.env.MEETING_SUM_HEARTBEAT_INTERVAL ?? 5000);
export const load: PageServerLoad = async ({ params }) => {
    return {
        heartbeatInterval: HEARTBEAT_INTERVAL
    };
};
