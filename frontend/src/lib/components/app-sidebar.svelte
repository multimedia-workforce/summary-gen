<script lang="ts" module>
    import Bot from "lucide-svelte/icons/bot";
    import ChartPie from "lucide-svelte/icons/chart-pie";
    import Frame from "lucide-svelte/icons/frame";
    import Map from "lucide-svelte/icons/map";
    import Settings2 from "lucide-svelte/icons/settings-2";
    import SquareTerminal from "lucide-svelte/icons/square-terminal";

    const data = {
        navMain: [
            {
                title: "Jobs",
                url: "/",
                icon: SquareTerminal,
                isActive: true,
                items: [
                    {
                        title: "Active",
                        url: "/",
                    },
                    {
                        title: "History",
                        url: "/history",
                    },
                ],
            },
            {
                title: "Settings",
                url: "settings",
                icon: Settings2,
            },
        ],
        projects: [
            {
                name: "Design Engineering",
                url: "#",
                icon: Frame,
            },
            {
                name: "Sales & Marketing",
                url: "#",
                icon: ChartPie,
            },
            {
                name: "Travel",
                url: "#",
                icon: Map,
            },
        ],
    };
</script>

<script lang="ts">
    import NavMain from "$lib/components/nav-main.svelte";
    import * as Sidebar from "$lib/components/ui/sidebar/index.js";
    import type { ComponentProps } from "svelte";

    let {
        ref = $bindable(null),
        collapsible = "icon",
        ...restProps
    }: ComponentProps<typeof Sidebar.Root> = $props();
</script>

<Sidebar.Root bind:ref {collapsible} {...restProps}>
    <Sidebar.Header>
        <Sidebar.Menu>
            <Sidebar.MenuItem>
                <Sidebar.MenuButton size="lg">
                    {#snippet child({ props })}
                        <a href="/" {...props}>
                            <div
                                class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg"
                            >
                                <Bot class="size-4" />
                            </div>
                            <div class="flex flex-col gap-0.5 leading-none">
                                <span class="font-semibold">SummaryGen</span>
                                <span class="">v1.0.0</span>
                            </div>
                        </a>
                    {/snippet}
                </Sidebar.MenuButton>
            </Sidebar.MenuItem>
        </Sidebar.Menu>
    </Sidebar.Header>
    <Sidebar.Content>
        <NavMain items={data.navMain} />
    </Sidebar.Content>
    <Sidebar.Footer></Sidebar.Footer>
    <Sidebar.Rail />
</Sidebar.Root>
