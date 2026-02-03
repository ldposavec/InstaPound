<script lang="ts">
    import type { Snippet } from 'svelte';
    import { AlertCircle, CheckCircle2, Info, XCircle } from 'lucide-svelte';

    interface Props {
        variant?: 'info' | 'success' | 'warning' | 'error';
        title?: string;
        dismissible?: boolean;
        class?: string;
        children: Snippet;
    }

    let { variant = 'info', title = '', dismissible = false, class: className = '', children }: Props = $props();
    let visible = $state(true);

    const variants = {
        info: {
            bg: 'bg-blue-50 dark:bg-blue-950/50',
            border: 'border-blue-200 dark:border-blue-800',
            text: 'text-blue-800 dark:text-blue-200',
            icon: Info
        },
        success: {
            bg: 'bg-emerald-50 dark:bg-emerald-950/50',
            border: 'border-emerald-200 dark:border-emerald-800',
            text: 'text-emerald-800 dark:text-emerald-200',
            icon: CheckCircle2
        },
        warning: {
            bg: 'bg-amber-50 dark:bg-amber-950/50',
            border: 'border-amber-200 dark:border-amber-800',
            text: 'text-amber-800 dark:text-amber-200',
            icon: AlertCircle
        },
        error: {
            bg: 'bg-red-50 dark:bg-red-950/50',
            border: 'border-red-200 dark:border-red-800',
            text: 'text-red-800 dark:text-red-200',
            icon: XCircle
        }
    };

    const IconComponent = variants[variant].icon;
</script>

{#if visible}
    <div
            class="flex items-start gap-3 rounded-lg border p-4 {variants[variant].bg} {variants[variant].border} {className}"
            role="alert"
    >
        <IconComponent class="mt-0.5 h-5 w-5 flex-shrink-0 {variants[variant].text}" />
        <div class="flex-1 {variants[variant].text}">
            {#if title}
                <h4 class="mb-1 font-medium">{title}</h4>
            {/if}
            <div class="text-sm">
                {@render children()}
            </div>
        </div>
        {#if dismissible}
            <button
                    onclick={() => (visible = false)}
                    class="rounded p-1 transition-colors hover:bg-black/5 dark:hover:bg-white/5 {variants[variant].text}"
            >
                <XCircle class="h-4 w-4" />
            </button>
        {/if}
    </div>
{/if}