<script lang="ts">
    interface Props {
        value?: string;
        label?: string;
        placeholder?: string;
        error?: string;
        disabled?: boolean;
        required?: boolean;
        rows?: number;
        class?: string;
        id?: string;
        name?: string;
    }

    let {
        value = $bindable(''),
        label = '',
        placeholder = '',
        error = '',
        disabled = false,
        required = false,
        rows = 4,
        class: className = '',
        id = '',
        name = ''
    }: Props = $props();

    const inputId = id || `textarea-${Math.random().toString(36).substr(2, 9)}`;
</script>

<div class="w-full">
    {#if label}
        <label for={inputId} class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
            {label}
            {#if required}<span class="text-red-500">*</span>{/if}
        </label>
    {/if}
    <textarea
            id={inputId}
            {name}
            {placeholder}
            bind:value
            {disabled}
            {required}
            {rows}
            class="w-full resize-none rounded-lg border bg-white px-4 py-2.5 text-slate-900 transition-all duration-200
            placeholder:text-slate-400 focus:outline-none focus:ring-2 disabled:cursor-not-allowed disabled:bg-slate-50
            disabled:text-slate-500 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500
		{error
			? 'border-red-300 focus:border-red-500 focus:ring-red-500/20'
			: 'border-slate-200 focus:border-violet-500 focus:ring-violet-500/20 dark:border-slate-700 dark:focus:border-violet-400'}
		{className}"
    ></textarea>
    {#if error}
        <p class="mt-1.5 text-sm text-red-600 dark:text-red-400">{error}</p>
    {/if}
</div>