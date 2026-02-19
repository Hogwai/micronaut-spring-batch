package com.hogwai.batch.core.runtime;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ExecutionContextTest {

    @Test
    void shouldStoreAndRetrieveString() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.putString("key", "value");
        assertThat(ctx.getString("key")).isEqualTo("value");
    }

    @Test
    void shouldStoreAndRetrieveLong() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.putLong("count", 42L);
        assertThat(ctx.getLong("count")).isEqualTo(42L);
    }

    @Test
    void shouldReturnNullForMissingKey() {
        ExecutionContext ctx = new ExecutionContext();
        assertThat(ctx.get("missing")).isNull();
    }

    @Test
    void shouldSupportContainsKey() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.put("key", "val");
        assertThat(ctx.containsKey("key")).isTrue();
        assertThat(ctx.containsKey("other")).isFalse();
    }

    @Test
    void shouldCopyFromAnotherContext() {
        ExecutionContext source = new ExecutionContext();
        source.putString("a", "1");
        source.putLong("b", 2L);

        ExecutionContext target = new ExecutionContext();
        target.putAll(source);
        assertThat(target.getString("a")).isEqualTo("1");
        assertThat(target.getLong("b")).isEqualTo(2L);
    }
}
