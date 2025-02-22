import { summarize, type SummarizePrompt } from '$lib/grpc/summarize';

const ESummarizeStatus = {
    PROCESSING: "processing",
    COMPLETED: "completed",
    ERROR: "error"
} as const;

export type SummarizeStatus = typeof ESummarizeStatus[keyof typeof ESummarizeStatus];
export type SummarizeResponse = {
    status: SummarizeStatus;
    result?: string;
}

export async function POST({ request }) {
    const summarizeRequest = await request.json() as SummarizePrompt;

    const stream = new ReadableStream({
        async start(controller) {
            try {
                // Send initial processing message
                const processingMessage: SummarizeResponse = { status: ESummarizeStatus.PROCESSING };
                controller.enqueue(btoa(JSON.stringify(processingMessage)) + '\n');

                // Perform the long-running gRPC call
                const result = await summarize(summarizeRequest);

                // Send the final result
                const completedMessage: SummarizeResponse = { status: ESummarizeStatus.COMPLETED, result };
                controller.enqueue(btoa(JSON.stringify(completedMessage)) + '\n');
                controller.close();
            } catch (error) {
                const errorMessage: SummarizeResponse = {
                    status: ESummarizeStatus.ERROR,
                    result: error instanceof Error ? error.message : String(error)
                };

                controller.enqueue(btoa(JSON.stringify(errorMessage)) + '\n');
                controller.close();
            }
        }
    });

    return new Response(stream, {
        headers: {
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache',
            'Connection': 'keep-alive'
        }
    });
};
