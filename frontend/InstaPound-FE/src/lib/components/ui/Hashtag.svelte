<script lang="ts">
    interface Props {
        tag: string;
        removable?: boolean;
        onremove?: () => void;
        class?: string;
    }
    let { tag, removable = false, onremove, class: className = '' }: Props = $props();
</script>

<span
        class="inline-flex items-center gap-1 rounded-full bg-violet-100 px-3 py-1 text-sm font-medium text-violet-700 dark:bg-violet-900/50 dark:text-violet-300 {className}"
>
	<span class="text-violet-500">#</span>
    {tag}
    {#if removable}
		<button
                onclick={onremove}
                class="-mr-1 ml-1 rounded-full p-0.5 transition-colors hover:bg-violet-200 dark:hover:bg-violet-800"
                aria-label="Remove hashtag"
        >
			<svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
			</svg>
		</button>
	{/if}
</span>