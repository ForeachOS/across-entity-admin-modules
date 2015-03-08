package com.foreach.across.modules.web.ui;

import java.util.*;

/**
 * POC.
 */
public class ViewElementGenerator implements Iterable<ViewElement>
{
	public static interface GeneratorCallback
	{
		ViewElement create( Object item );
	}

	private class GenerationIterator implements Iterator<ViewElement>
	{
		private int position = 0;
		private List<Object> items;
		private List<ViewElement> generated;
		private GeneratorCallback callback;

		public GenerationIterator( List<Object> items,
		                           List<ViewElement> generated,
		                           GeneratorCallback callback ) {
			this.items = items;
			this.generated = generated;
			this.callback = callback;
		}

		@Override
		public boolean hasNext() {
			return position < items.size();
		}

		@Override
		public ViewElement next() {
			if ( generated.size() <= position ) {
				generated.add( callback.create( items.get( position )) );
			}

			return generated.get( position++ );
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException( "Remove is not supported on generation iterators." );
		}
	}

	private List<Object> items = Collections.emptyList();
	private List<ViewElement> generated = Collections.emptyList();
	private GeneratorCallback callback;

	public void setItems( Collection<Object> items ) {
		this.items = new ArrayList<>( items );
		generated = new ArrayList<>( items.size() );
	}

	public void setCallback( GeneratorCallback callback ) {
		this.callback = callback;
	}

	@Override
	public Iterator<ViewElement> iterator() {
		return new GenerationIterator( items, generated, callback );
	}
}
