<script lang="ts">
    import * as Collapsible from "$lib/components/ui/collapsible/index.js";
    import * as Sidebar from "$lib/components/ui/sidebar/index.js";
    import ChevronRight from "lucide-svelte/icons/chevron-right";

    let {
        items,
    }: {
        items: {
            title: string;
            url: string;
            // this should be `Component` after lucide-svelte updates types
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            icon?: any;
            isActive?: boolean;
            items?: {
                title: string;
                url: string;
            }[];
        }[];
    } = $props();
</script>

<Sidebar.Group>
    <Sidebar.GroupLabel>Generation</Sidebar.GroupLabel>
    <Sidebar.Menu>
        {#each items as mainItem (mainItem.title)}
            <Collapsible.Root
                    open={mainItem.isActive}
                    class="group/collapsible"
            >
                {#snippet child({props})}
                    <Sidebar.MenuItem {...props}>
                        {#if mainItem.items}
                            <Collapsible.Trigger>
                                {#snippet child({props})}
                                    <Sidebar.MenuButton {...props}>
                                        {#if mainItem.icon}
                                            <mainItem.icon/>
                                        {/if}
                                        <span>{mainItem.title}</span>
                                        <ChevronRight
                                                class="ml-auto transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                                        />
                                    </Sidebar.MenuButton>
                                {/snippet}
                            </Collapsible.Trigger>
                            <Collapsible.Content>
                                {#if mainItem.items}
                                    <Sidebar.MenuSub>
                                        {#each mainItem.items as subItem (subItem.title)}
                                            <Sidebar.MenuSubItem>
                                                <Sidebar.MenuSubButton>
                                                    {#snippet child({props})}
                                                        <a
                                                                href={subItem.url}
                                                                {...props}
                                                        >
														<span
                                                        >{subItem.title}</span
                                                        >
                                                        </a>
                                                    {/snippet}
                                                </Sidebar.MenuSubButton>
                                            </Sidebar.MenuSubItem>
                                        {/each}
                                    </Sidebar.MenuSub>
                                {/if}
                            </Collapsible.Content>
                        {:else}
                            <Sidebar.MenuButton {...props}>
                                {#if mainItem.icon}
                                    <mainItem.icon/>
                                {/if}
                                <a href={mainItem.url}
                                   {...props}
                                >
                                    <span>{mainItem.title}</span>
                                </a>
                            </Sidebar.MenuButton>
                        {/if}

                    </Sidebar.MenuItem>
                {/snippet}
            </Collapsible.Root>
        {/each}
    </Sidebar.Menu>
</Sidebar.Group>
