<script lang="ts">
    import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
    import { Card, CardContent } from '@/components/ui/card';
    import { Label } from '@/components/ui/label';
    import { Input } from '@/components/ui/input';
    import { Button } from '@/components/ui/button';
    import { goto } from '$app/navigation';

    let mode: 'login' | 'register' = 'login';
    let username = '';
    let password = '';
    let error = '';

    async function handleSubmit() {
        error = '';
        console.log('🔄 Submitting', mode, { username, password });

        const res = await fetch(`/api/auth/${mode}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        console.log(res);

        if (res.ok) {
            // Allow time for the cookie to be set
            setTimeout(() => {
                goto('/');
            }, 100);
        } else {
            const data = await res.json();
            error = data.error || 'Something went wrong';
        }
    }
</script>

<div class="flex items-center justify-center min-h-screen bg-gray-50 px-4">
    <div class="w-full max-w-md">
        <h1 class="text-center text-2xl font-semibold mb-6">
            Please login or register to use <span class="text-blue-600">Smart Session Summary</span>
        </h1>
        <Card>
            <Tabs bind:value={mode} class="p-4">
                <TabsList class="grid grid-cols-2 mb-4">
                    <TabsTrigger type="button" value="login">Login</TabsTrigger>
                    <TabsTrigger type="button" value="register">Register</TabsTrigger>
                </TabsList>

                <TabsContent value="login">
                    <CardContent class="space-y-4">
                        <form on:submit|preventDefault={handleSubmit} class="space-y-4">
                            <div>
                                <Label for="login-username">Username</Label>
                                <Input id="login-username" bind:value={username} placeholder="johndoe" />
                            </div>
                            <div>
                                <Label for="login-password">Password</Label>
                                <Input id="login-password" type="password" bind:value={password} />
                            </div>
                            {#if error}
                                <p class="text-red-500 text-sm">{error}</p>
                            {/if}
                            <Button type="submit" class="w-full">Login</Button>
                        </form>
                    </CardContent>
                </TabsContent>

                <TabsContent value="register">
                    <CardContent class="space-y-4">
                        <form on:submit|preventDefault={handleSubmit} class="space-y-4">
                            <div>
                                <Label for="register-username">Username</Label>
                                <Input id="register-username" bind:value={username} placeholder="johndoe" />
                            </div>
                            <div>
                                <Label for="register-password">Password</Label>
                                <Input id="register-password" type="password" bind:value={password} />
                            </div>
                            {#if error}
                                <p class="text-red-500 text-sm">{error}</p>
                            {/if}
                            <Button type="submit" class="w-full">Register</Button>
                        </form>
                    </CardContent>
                </TabsContent>
            </Tabs>
        </Card>
    </div>
</div>
