/*******************************************************************************
 * Copyright 2015 uniVocity Software Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.univocity.parsers.common.processor;

import com.univocity.parsers.common.CommonSettings;
import com.univocity.parsers.common.CommonWriterSettings;

/**
 * A special {@link RowWriterProcessor} implementation that combines and allows switching among different
 * RowWriterProcessors. Concrete implementations of this class
 * are expected to implement the {@code switchRowProcessor(T)} method and analyze the input row
 * to determine whether or not the current {@link RowWriterProcessor} implementation must be changed to handle a special
 * circumstance (determined by the concrete implementation) such as a different row format.
 *
 * When the row writer processor is switched, the {@link #rowProcessorSwitched(RowWriterProcessor, RowWriterProcessor)}
 * will be called, and must be overridden, to notify the change to the user.
 */
public abstract class RowWriterProcessorSwitch<T> implements RowWriterProcessor<T> {

	private RowWriterProcessor<T> selectedRowWriterProcessor = null;

	/**
	 * Analyzes an output row to determine whether or not the row writer processor implementation must be changed
	 * @param row a record with data to be written to the output
	 * @return the row writer processor implementation to use. If it is not the same as the one used by the previous row,
	 * the returned row writer processor will be used, and the {@link #rowProcessorSwitched(RowWriterProcessor, RowWriterProcessor)} method
	 * will be called.
	 */
	protected abstract RowWriterProcessor<T> switchRowProcessor(T row);

	/**
	 * Returns the headers in use by the current row writer processor implementation, which can vary among row writer processors.
	 * If {@code null}, the headers defined in {@link CommonWriterSettings#getHeaders()} will be returned.
	 * @return the current sequence of headers to use.
	 */
	protected String[] getHeaders() {
		return null;
	}

	/**
	 * Returns the indexes in use by the current row writer processor implementation, which can vary among row writer processors.
	 * If {@code null}, the indexes of fields that have been selected using {@link CommonSettings#selectFields(String...)}
	 * or {@link CommonSettings#selectIndexes(Integer...)} will be returned.
	 * @return the current sequence of indexes to use.
	 */
	protected int[] getIndexes() {
		return null;
	}

	/**
	 * Notifies a change of row writer processor implementation. Users are expected to override this method to receive the notification.
	 * @param from the row writer processor previously in use
	 * @param to the new row writer processor to use to continue processing the output rows.
	 */
	public void rowProcessorSwitched(RowWriterProcessor<T> from, RowWriterProcessor<T> to) {
	}

	@Override
	public Object[] write(T input, String[] headers, int[] indexesToWrite) {
		RowWriterProcessor<T> processor = switchRowProcessor(input);
		if (processor == null) {
			return null;
		}
		if (processor != selectedRowWriterProcessor) {
			rowProcessorSwitched(selectedRowWriterProcessor, processor);
			selectedRowWriterProcessor = processor;
		}

		String[] headersToUse = getHeaders();
		int[] indexesToUse = getIndexes();

		headersToUse = headersToUse == null ? headers : headersToUse;
		indexesToUse = indexesToUse == null ? indexesToWrite : indexesToUse;

		return selectedRowWriterProcessor.write(input, headersToUse, indexesToUse);
	}
}
