package com.hogwai.batch.core.runtime;

public class StepContribution {
    private long readCount;
    private long writeCount;
    private long filterCount;
    private long skipCountInRead;
    private long skipCountInProcess;
    private long skipCountInWrite;
    private ExitStatus exitStatus = ExitStatus.COMPLETED;

    public void incrementReadCount() { readCount++; }
    public void incrementWriteCount(long count) { writeCount += count; }
    public void incrementFilterCount() { filterCount++; }
    public void incrementSkipCountInRead() { skipCountInRead++; }
    public void incrementSkipCountInProcess() { skipCountInProcess++; }
    public void incrementSkipCountInWrite() { skipCountInWrite++; }

    public long getReadCount() { return readCount; }
    public long getWriteCount() { return writeCount; }
    public long getFilterCount() { return filterCount; }
    public long getSkipCount() { return skipCountInRead + skipCountInProcess + skipCountInWrite; }
    public long getSkipCountInRead() { return skipCountInRead; }
    public long getSkipCountInProcess() { return skipCountInProcess; }
    public long getSkipCountInWrite() { return skipCountInWrite; }

    public ExitStatus getExitStatus() { return exitStatus; }
    public void setExitStatus(ExitStatus exitStatus) { this.exitStatus = exitStatus; }
}
