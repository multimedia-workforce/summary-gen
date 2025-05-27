<script lang="ts">
    import { onMount } from 'svelte';
    let prompt = '';
    let status = '';

    onMount(async () => {
      const response = await fetch('/api/settings');
      if (response.ok) {
        const data = await response.json();
        prompt = data.prompt;
      }
    });

    async function save() {
      const response = await fetch('/api/settings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt })
      });
      status = response.ok ? 'Settings saved!' : 'Error saving settings';
      setTimeout(() => (status = ''), 3000);
    }
  </script>

<div class="hidden space-y-6 px-10 pb-10 md:block">
  <div class="space-y-0.5">
      <h2 class="text-2xl font-bold tracking-tight">Settings</h2>
      <p class="text-muted-foreground">Manage your account settings.</p>
  </div>
  <form on:submit|preventDefault={save} class="flex flex-col space-y-8">
      <label>
          <div class="block font-semibold">Custom Prompt</div>
          <textarea bind:value={prompt} placeholder="Summarize the following transcript." rows={6} class="w-full p-2 border rounded"></textarea>
      </label>
      <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded">Save</button>
      {#if status}
          <p class="mt-2 text-sm text-green-600">{status}</p>
      {/if}
  </form>
</div>
