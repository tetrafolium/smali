/*
 * Copyright 2013, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.writer;

import com.google.common.collect.Ordering;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.base.BaseAnnotationElement;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodHandleReference;
import org.jf.dexlib2.iface.reference.MethodReference;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;

public abstract class EncodedValueWriter<StringKey, TypeKey, FieldRefKey extends FieldReference,
        MethodRefKey extends MethodReference, AnnotationElement extends org.jf.dexlib2.iface.AnnotationElement,
        ProtoRefKey, MethodHandleKey extends MethodHandleReference, EncodedValue> {
    @Nonnull private final DexDataWriter writer;
    @Nonnull private final StringSection<StringKey, ?> stringSection;
    @Nonnull private final TypeSection<?, TypeKey, ?> typeSection;
    @Nonnull private final FieldSection<?, ?, FieldRefKey, ?> fieldSection;
    @Nonnull private final MethodSection<?, ?, ?, MethodRefKey, ?> methodSection;
    @Nonnull private final ProtoSection<?, ?, ProtoRefKey, ?> protoSection;
    @Nonnull private final MethodHandleSection<MethodHandleKey, ?, ?> methodHandleSection;
    @Nonnull private final AnnotationSection<StringKey, TypeKey, ?, AnnotationElement, EncodedValue> annotationSection;

    public EncodedValueWriter(
            final @Nonnull DexDataWriter writer,
            final @Nonnull StringSection<StringKey, ?> stringSection,
            final @Nonnull TypeSection<?, TypeKey, ?> typeSection,
            final @Nonnull FieldSection<?, ?, FieldRefKey, ?> fieldSection,
            final @Nonnull MethodSection<?, ?, ?, MethodRefKey, ?> methodSection,
            final ProtoSection<?, ?, ProtoRefKey, ?> protoSection,
            final MethodHandleSection<MethodHandleKey, ?, ?> methodHandleSection,
            final @Nonnull AnnotationSection<StringKey, TypeKey, ?, AnnotationElement, EncodedValue> annotationSection) {
        this.writer = writer;
        this.stringSection = stringSection;
        this.typeSection = typeSection;
        this.fieldSection = fieldSection;
        this.methodSection = methodSection;
        this.protoSection = protoSection;
        this.methodHandleSection = methodHandleSection;
        this.annotationSection = annotationSection;
    }

    protected abstract void writeEncodedValue(@Nonnull EncodedValue encodedValue) throws IOException;

    public void writeAnnotation(final TypeKey annotationType,
                                final Collection<? extends AnnotationElement> elements) throws IOException {
        writer.writeEncodedValueHeader(ValueType.ANNOTATION, 0);
        writer.writeUleb128(typeSection.getItemIndex(annotationType));
        writer.writeUleb128(elements.size());

        Collection<? extends AnnotationElement> sortedElements = Ordering.from(BaseAnnotationElement.BY_NAME)
                .immutableSortedCopy(elements);

        for (AnnotationElement element: sortedElements) {
            writer.writeUleb128(stringSection.getItemIndex(annotationSection.getElementName(element)));
            writeEncodedValue(annotationSection.getElementValue(element));
        }
    }

    public void writeArray(final Collection<? extends EncodedValue> elements) throws IOException {
        writer.writeEncodedValueHeader(ValueType.ARRAY, 0);
        writer.writeUleb128(elements.size());
        for (EncodedValue element: elements) {
            writeEncodedValue(element);
        }
    }

    public void writeBoolean(final boolean value) throws IOException {
        writer.writeEncodedValueHeader(ValueType.BOOLEAN, value ? 1 : 0);
    }

    public void writeByte(final byte value) throws IOException {
        writer.writeEncodedInt(ValueType.BYTE, value);
    }

    public void writeChar(final char value) throws IOException {
        writer.writeEncodedUint(ValueType.CHAR, value);
    }

    public void writeDouble(final double value) throws IOException {
        writer.writeEncodedDouble(ValueType.DOUBLE, value);
    }

    public void writeEnum(final @Nonnull FieldRefKey value) throws IOException {
        writer.writeEncodedUint(ValueType.ENUM, fieldSection.getItemIndex(value));
    }
    
    public void writeField(final @Nonnull FieldRefKey value) throws IOException {
        writer.writeEncodedUint(ValueType.FIELD, fieldSection.getItemIndex(value));
    }

    public void writeFloat(final float value) throws IOException {
        writer.writeEncodedFloat(ValueType.FLOAT, value);
    }

    public void writeInt(final int value) throws IOException {
        writer.writeEncodedInt(ValueType.INT, value);
    }

    public void writeLong(final long value) throws IOException {
        writer.writeEncodedLong(ValueType.LONG, value);
    }

    public void writeMethod(final @Nonnull MethodRefKey value) throws IOException {
        writer.writeEncodedUint(ValueType.METHOD, methodSection.getItemIndex(value));
    }

    public void writeNull() throws IOException {
        writer.write(ValueType.NULL);
    }

    public void writeShort(final int value) throws IOException {
        writer.writeEncodedInt(ValueType.SHORT, value);
    }

    public void writeString(final @Nonnull StringKey value) throws IOException {
        writer.writeEncodedUint(ValueType.STRING, stringSection.getItemIndex(value));
    }

    public void writeType(final @Nonnull TypeKey value) throws IOException {
        writer.writeEncodedUint(ValueType.TYPE, typeSection.getItemIndex(value));
    }

    public void writeMethodType(final @Nonnull ProtoRefKey value) throws IOException {
        writer.writeEncodedUint(ValueType.METHOD_TYPE, protoSection.getItemIndex(value));
    }

    public void writeMethodHandle(final @Nonnull MethodHandleKey value) throws IOException {
        writer.writeEncodedUint(ValueType.METHOD_HANDLE, methodHandleSection.getItemIndex(value));
    }
}
