<script lang="ts">
    interface Props {
        type?: 'text' | 'email' | 'password' | 'number' | 'search' | 'date';
        placeholder?: string;
        value?: string | number;
        label?: string;
        error?: string;
        disabled?: boolean;
        required?: boolean;
        class?: string;
        id?: string;
        name?: string;
        oninput?: (e: Event) => void;
        onchange?: (e: Event) => void;
        onkeydown?: (e: KeyboardEvent) => void;
    }

    let {
        type = 'text',
        placeholder = '',
        value = $bindable(''),
        label = '',
        error = '',
        disabled = false,
        required = false,
        class: className = '',
        id = '',
        name = '',
        oninput,
        onchange
    }: Props = $props();

    // const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;
    const inputId = id || `input-${Math.random().toString(36).substring(2, 9)}`;
</script>

<div class="w-full">
    {#if label}
        <label for={inputId} class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
            {label}
            {#if required}<span class="text-red-500">*</span>{/if}
        </label>
    {/if}
    <input
            {type}
            id={inputId}
            {name}
            {placeholder}
            bind:value
            {disabled}
            {required}
            {oninput}
            {onchange}
            class="w-full rounded-lg border bg-white px-4 py-2.5 text-slate-900 transition-all duration-200
            placeholder:text-slate-400 focus:outline-none focus:ring-2 disabled:cursor-not-allowed
            disabled:bg-slate-50 disabled:text-slate-500 dark:bg-slate-900 dark:text-slate-100
            dark:placeholder:text-slate-500
		{error
			? 'border-red-300 focus:border-red-500 focus:ring-red-500/20'
			: 'border-slate-200 focus:border-violet-500 focus:ring-violet-500/20 dark:border-slate-700 dark:focus:border-violet-400'}
		{className}"
    />
    {#if error}
        <p class="mt-1.5 text-sm text-red-600 dark:text-red-400">{error}</p>
    {/if}
</div>