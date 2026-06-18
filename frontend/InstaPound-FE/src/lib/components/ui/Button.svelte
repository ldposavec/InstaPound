<script lang="ts">
    import type { Snippet } from 'svelte';

    interface Props {
        variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
        size?: 'sm' | 'md' | 'lg';
        disabled?: boolean;
        loading?: boolean;
        type?: 'button' | 'submit' | 'reset';
        class?: string;
        onclick?: (e: MouseEvent) => void;
        children?: Snippet;
    }

    let {
        variant = 'primary',
        size = 'md',
        disabled = false,
        loading = false,
        type = 'button',
        class: className = '',
        onclick,
        children
    }: Props = $props();

    const baseClasses = 'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-200' +
        ' focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

    const variants = {
        primary:
            'bg-gradient-to-r from-violet-600 to-indigo-600 text-white hover:from-violet-700 hover:to-indigo-700 ' +
            'focus:ring-violet-500 shadow-lg shadow-violet-500/25',
        secondary:
            'bg-slate-100 text-slate-900 hover:bg-slate-200 focus:ring-slate-500 dark:bg-slate-800 dark:text-slate-100 ' +
            'dark:hover:bg-slate-700',
        danger:
            'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500 shadow-lg shadow-red-500/25',
        ghost:
            'bg-transparent text-slate-700 hover:bg-slate-100 focus:ring-slate-500 dark:text-slate-300 dark:hover:bg-slate-800',
        outline:
            'border-2 border-violet-600 text-violet-600 hover:bg-violet-50 focus:ring-violet-500 dark:border-violet-400 ' +
            'dark:text-violet-400 dark:hover:bg-violet-950'
    };

    const sizes = {
        sm: 'px-3 py-1.5 text-sm gap-1.5',
        md: 'px-4 py-2 text-sm gap-2',
        lg: 'px-6 py-3 text-base gap-2',
    };
</script>

<button {type} disabled={disabled || loading} class="{baseClasses} {variants[variant]} {sizes[size]} {className}"
        {onclick}>
    {#if loading}
        <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" />
            <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
        </svg>
    {/if}
    {@render children?.()}
</button>