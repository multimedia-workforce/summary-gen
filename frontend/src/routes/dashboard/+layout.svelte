<script lang="ts">
    import * as Sidebar from "@/components/ui/sidebar";
    import AppSidebar from "@/components/app-sidebar.svelte";
    import {Separator} from "@/components/ui/separator";
    import * as Breadcrumb from "@/components/ui/breadcrumb";

    let {children, data} = $props();
</script>

<Sidebar.Provider>
    <AppSidebar id={data.user?.id} username={data.user?.username}/>
    <Sidebar.Inset>
        <header class="flex h-16 shrink-0 items-center gap-2">
            <div class="flex items-center gap-2 px-4">
                <Sidebar.Trigger class="-ml-1"/>
                <Separator class="mr-2 h-4" orientation="vertical"/>
                <Breadcrumb.Root>
                    <Breadcrumb.List>
                        <Breadcrumb.Item class="hidden md:block">
                            <Breadcrumb.Link href="#"
                            >Summary Generation
                            </Breadcrumb.Link>
                        </Breadcrumb.Item>
                    </Breadcrumb.List>
                </Breadcrumb.Root>
            </div>
        </header>
        {@render children?.()}
    </Sidebar.Inset>
</Sidebar.Provider>
