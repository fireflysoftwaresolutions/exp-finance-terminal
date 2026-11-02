/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.openapi.impl;

import static org.assertj.core.api.Assertions.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OriginalProducerSdkInputFixtureTest {
    @TempDir Path root;
    private Properties setup(byte[] original)throws Exception {
        var web=root.resolve("exp-finance-terminal-web");Files.createDirectories(web.resolve("target/staff-posting-producer"));
        var canonical=root.resolve("exp-finance-terminal-sdk/src/main/resources/openapi.yml");Files.createDirectories(canonical.getParent());Files.write(canonical,original);
        var properties=new Properties();properties.setProperty("staff.posting.fixture.sdk-owned-hosted","true");properties.setProperty("staff.posting.fixture.gate","a".repeat(64));properties.setProperty("staff.posting.fixture.sdk-source-root",root.toString());
        properties.setProperty("staff.posting.fixture.sdk-canonical-input",canonical.toString());
        properties.setProperty("staff.posting.fixture.sdk-canonical-preimage-sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(original)));return properties;
    }
    @Test void explicitOwnedFixtureCopiesExactRawBytesAndRetainsThePreimageReceipt()throws Exception {
        var original="retained original input\n".getBytes();var raw="actual emitted raw YAML\n".getBytes();var properties=setup(original);
        OriginalProducerSdkInputFixtureImpl.write(raw,root.resolve("exp-finance-terminal-web"),properties);
        assertThat(Files.readAllBytes(Path.of(properties.getProperty("staff.posting.fixture.sdk-canonical-input")))).isEqualTo(raw);
        assertThat(Files.readString(root.resolve("exp-finance-terminal-web/target/staff-posting-producer/sdk-canonicalization.json"))).contains("preimageSha256","actualRawProducerSha256");
    }
    @Test void defaultOffAndWrongPreimageOrArbitraryPathCannotWriteTheCanonicalInput()throws Exception {
        var original="retained original input\n".getBytes();var properties=setup(original);var web=root.resolve("exp-finance-terminal-web");
        OriginalProducerSdkInputFixtureImpl.write("unrequested".getBytes(),web,new Properties());
        var target=Path.of(properties.getProperty("staff.posting.fixture.sdk-canonical-input"));assertThat(Files.readAllBytes(target)).isEqualTo(original);
        var missingMarker=(Properties)properties.clone();missingMarker.remove("staff.posting.fixture.sdk-owned-hosted");
        assertThatThrownBy(()->OriginalProducerSdkInputFixtureImpl.write("bad".getBytes(),web,missingMarker)).isInstanceOf(IllegalStateException.class);
        properties.setProperty("staff.posting.fixture.sdk-canonical-preimage-sha256","b".repeat(64));
        assertThatThrownBy(()->OriginalProducerSdkInputFixtureImpl.write("bad".getBytes(),web,properties)).isInstanceOf(IllegalStateException.class);
        properties.setProperty("staff.posting.fixture.sdk-canonical-input",root.resolve("unowned.yml").toString());
        assertThatThrownBy(()->OriginalProducerSdkInputFixtureImpl.write("bad".getBytes(),web,properties)).isInstanceOf(IllegalStateException.class);
        assertThat(Files.readAllBytes(target)).isEqualTo(original);assertThat(root.resolve("unowned.yml")).doesNotExist();
    }
    @Test void aSymlinkCannotRedirectEvenTheDeclaredCanonicalAlias()throws Exception {
        var original="retained original input\n".getBytes();var properties=setup(original);var target=Path.of(properties.getProperty("staff.posting.fixture.sdk-canonical-input"));
        var outside=root.resolve("retained.yml");Files.write(outside,original);Files.delete(target);Files.createSymbolicLink(target,outside);
        assertThatThrownBy(()->OriginalProducerSdkInputFixtureImpl.write("bad".getBytes(),root.resolve("exp-finance-terminal-web"),properties)).isInstanceOf(IllegalStateException.class);
        assertThat(Files.readAllBytes(outside)).isEqualTo(original);
    }
}
