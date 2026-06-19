<script lang="ts">
    interface Props {
        currentPage: number;
        totalPages: number;
        onPageChange: (page: number) => void;
        class?: string;
    }

    let { currentPage, totalPages, onPageChange, class: className = '' }: Props = $props();

    function getVisiblePages(): (number | 'ellipsis')[] {
        const pages: (number | 'ellipsis')[] = [];
        const maxVisible = 5;
        if (totalPages <= maxVisible) {
            for (let i = 0; i < totalPages; i++) pages.push(i);
        } else {
            pages.push(0);
            if (currentPage > 2) {
                pages.push('ellipsis');
            }
            const start = Math.max(1, currentPage - 1);
            const end = Math.min(totalPages - 2, currentPage + 1);
            for (let i = start; i <= end; i++) {
                if (!pages.includes(i)) pages.push(i);
            }
            if (currentPage < totalPages - 3) {
                pages.push('ellipsis');
            }
            if (!pages.includes(totalPages - 1)) {
                pages.push(totalPages - 1);
            }
        }
        return pages;
    }
</script>

{#if totalPages > 1}
    <nav class="flex items-center justify-center gap-1 {className}" aria-label="Pagination">
        <button
                onclick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
                class="rounded-lg p-2 text-slate-600 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50 dark:text-slate-400 dark:hover:bg-slate-800"
                aria-label="Previous page"
        >
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
        </button>
        {#each getVisiblePages() as page}
            {#if page === 'ellipsis'}
                <span class="px-2 text-slate-400">...</span>
            {:else}
                <button
                        onclick={() => onPageChange(page)}
                        class="min-w-[40px] rounded-lg px-3 py-2 text-sm font-medium transition-colors
					{page === currentPage
						? 'bg-violet-600 text-white'
						: 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800'}"
                >
                    {page + 1}
                </button>
            {/if}
        {/each}
        <button
                onclick={() => onPageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                class="rounded-lg p-2 text-slate-600 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50 dark:text-slate-400 dark:hover:bg-slate-800"
                aria-label="Next page"
        >
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
        </button>
    </nav>
{/if}