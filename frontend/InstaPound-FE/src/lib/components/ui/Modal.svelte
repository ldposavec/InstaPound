<script lang="ts">
    import type { Snippet } from "svelte";
    import { X } from 'lucide-svelte';

    interface Props {
        open: boolean;
        title?: string;
        size?: 'sm' | 'md' | 'lg' | 'xl';
        onclose?: () => void;
        children: Snippet;
    }

    let { open = $bindable(false), title = '', size = 'md', onclose, children }: Props = $props();

    const sizes = {
        sm: 'max-w-sm',
        md: 'max-w-md',
        lg: 'max-w-lg',
        xl: 'max-w-xl'
    };

    function handleBackdropClick(e: MouseEvent) {
        if (e.target === e.currentTarget) {
            open = false;
            onclose?.();
        }
    }

    function handleClose() {
        open = false;
        onclose?.();
    }

    function handleKeydown(e: KeyboardEvent) {
        if (e.key === 'Escape') {
            handleClose();
        }
    }
</script>

<svelte:window onkeydown={handleKeydown} />

{#if open}
    <div
            class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm"
            role="dialog"
            aria-modal="true"
            aria-label={title || 'Dialog'}
            tabindex="-1"
            onclick={handleBackdropClick}
            onkeydown={handleKeydown}
    >
        <div
                class="w-full animate-in fade-in zoom-in-95 rounded-2xl bg-white p-6 shadow-2xl duration-200 dark:bg-slate-900 {sizes[
				size
			]}"
        >
            {#if title}
                <div class="mb-4 flex items-center justify-between">
                    <h2 class="text-xl font-semibold text-slate-900 dark:text-white">{title}</h2>
                    <button
                            onclick={handleClose}
                            class="rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800 dark:hover:text-slate-300"
                    >
                        <X class="h-5 w-5" />
                    </button>
                </div>
            {/if}
            {@render children()}
        </div>
    </div>
{/if}

<style>
    @keyframes fade-in {
        from {
            opacity: 0;
        }
        to {
            opacity: 1;
        }
    }

    @keyframes zoom-in-95 {
        from {
            transform: scale(0.95);
        }
        to {
            transform: scale(1);
        }
    }

    .animate-in {
        animation: fade-in 0.2s ease-out, zoom-in-95 0.2s ease-out;
    }
</style>