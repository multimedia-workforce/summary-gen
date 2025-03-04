<script lang="ts">
    import { onMount } from "svelte";
    import { Sun, Moon } from "lucide-svelte";

    type Props = {
        size: number;
    };

    let { size }: Props = $props();
    let isDarkMode = $state(false);

    // Load saved theme preference
    onMount(() => {
        const savedTheme = localStorage.getItem("theme");
        isDarkMode = savedTheme === "dark";
        updateTheme();
    });

    function toggleTheme() {
        isDarkMode = !isDarkMode;
        updateTheme();
    }

    function updateTheme() {
        const htmlElement = document.documentElement;
        if (isDarkMode) {
            htmlElement.classList.add("dark");
            localStorage.setItem("theme", "dark");
        } else {
            htmlElement.classList.remove("dark");
            localStorage.setItem("theme", "light");
        }
    }
</script>

<button onclick={toggleTheme} aria-label="Toggle Dark Mode">
    {#if isDarkMode}
        <Sun {size} />
    {:else}
        <Moon {size} />
    {/if}
</button>
