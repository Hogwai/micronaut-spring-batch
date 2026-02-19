package com.hogwai.batch.core.runtime;

/**
 * Accumulates read, write, filter, and skip metrics during chunk processing.
 * Applied to the parent {@link StepExecution} after each chunk completes.
 */
public class StepContribution {
    private long readCount;
    private long writeCount;
    private long filterCount;
    private long skipCountInRead;
    private long skipCountInProcess;
    private long skipCountInWrite;
    private ExitStatus exitStatus = ExitStatus.COMPLETED;

    /** Increments the read count by one. */
    public void incrementReadCount() { readCount++; }

    /**
     * Increments the write count by the given amount.
     *
     * @param count the number of items written
     */
    public void incrementWriteCount(long count) { writeCount += count; }

    /** Increments the filter count by one. */
    public void incrementFilterCount() { filterCount++; }

    /** Increments the skip-during-read count by one. */
    public void incrementSkipCountInRead() { skipCountInRead++; }

    /** Increments the skip-during-process count by one. */
    public void incrementSkipCountInProcess() { skipCountInProcess++; }

    /** Increments the skip-during-write count by one. */
    public void incrementSkipCountInWrite() { skipCountInWrite++; }

    /** @return the total number of items read */
    public long getReadCount() { return readCount; }

    /** @return the total number of items written */
    public long getWriteCount() { return writeCount; }

    /** @return the total number of items filtered out */
    public long getFilterCount() { return filterCount; }

    /** @return the total number of skipped items across all phases */
    public long getSkipCount() { return skipCountInRead + skipCountInProcess + skipCountInWrite; }

    /** @return the number of items skipped during the read phase */
    public long getSkipCountInRead() { return skipCountInRead; }

    /** @return the number of items skipped during the process phase */
    public long getSkipCountInProcess() { return skipCountInProcess; }

    /** @return the number of items skipped during the write phase */
    public long getSkipCountInWrite() { return skipCountInWrite; }

    /** @return the current exit status for this contribution */
    public ExitStatus getExitStatus() { return exitStatus; }

    /**
     * Sets the exit status for this contribution.
     *
     * @param exitStatus the exit status to set
     */
    public void setExitStatus(ExitStatus exitStatus) { this.exitStatus = exitStatus; }
}
