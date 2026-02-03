<script lang="ts">
    import type { Snippet } from 'svelte';

    interface Props {
        variant?: 'default' | 'elevated' | 'bordered';
        padding?: 'none' | 'sm' | 'md' | 'lg';
        class?: string;
        children: Snippet;
    }

    let { variant = 'default', padding = 'md', class: className = '', children }: Props = $props();

    const variants = {
        default: 'bg-white dark:bg-slate-900',
        elevated: 'bg-white dark:bg-slate-900 shadow-xl shadow-slate-200/50 dark:shadow-slate-900/50',
        bordered: 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800'
    };

    const paddings = {
        none: '',
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8'
    };
</script>

<div class="rounded-2xl {variants[variant]} {paddings[padding]} {className}">
    {@render children()}
</div>