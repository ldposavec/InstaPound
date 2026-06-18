<script lang="ts">
    import type { Snippet } from 'svelte';

    interface Option {
        value: string;
        label: string;
    }

    interface Props {
        value?: string;
        options: Option[];
        label?: string;
        error?: string;
        disabled?: boolean;
        required?: boolean;
        class?: string;
        id?: string;
        name?: string;
        placeholder?: string;
        onchange?: (e: Event) => void;
    }

    let {
        value = $bindable(''),
        options,
        label = '',
        error = '',
        disabled = false,
        required = false,
        class: className = '',
        id = '',
        name = '',
        placeholder = 'Select an option',
        onchange
    }: Props = $props();

    const inputId = $derived(id || `select-${Math.random().toString(36).substring(2, 9)}`);
</script>

<div class="w-full">
    {#if label}
        <label for={inputId} class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
            {label}
            {#if required}<span class="text-red-500">*</span>{/if}
        </label>
    {/if}
    <select
            id={inputId}
            {name}
            bind:value
            {disabled}
            {required}
            {onchange}
            class="w-full appearance-none rounded-lg border bg-white px-4 py-2.5 pr-10 text-slate-900 transition-all
            duration-200 focus:outline-none focus:ring-2 disabled:cursor-not-allowed disabled:bg-slate-50
            disabled:text-slate-500 dark:bg-slate-900 dark:text-slate-100
		{error
			? 'border-red-300 focus:border-red-500 focus:ring-red-500/20'
			: 'border-slate-200 focus:border-violet-500 focus:ring-violet-500/20 dark:border-slate-700 dark:focus:border-violet-400'}
		{className}"
            style="background-image: url('data:image/svg+xml,%3Csvg xmlns=%27http://www.w3.org/2000/svg%27 fill=%27none%27 viewBox=%270 0 20 20%27%3E%3Cpath stroke=%27%236b7280%27 stroke-linecap=%27round%27 stroke-linejoin=%27round%27 stroke-width=%271.5%27 d=%27m6 8 4 4 4-4%27/%3E%3C/svg%3E'); background-position: right 0.5rem center; background-repeat: no-repeat; background-size: 1.5em 1.5em;"
    >
        {#if placeholder}
            <option value="" disabled>{placeholder}</option>
        {/if}
        {#each options as option}
            <option value={option.value}>{option.label}</option>
        {/each}
    </select>
    {#if error}
        <p class="mt-1.5 text-sm text-red-600 dark:text-red-400">{error}</p>
    {/if}
</div>