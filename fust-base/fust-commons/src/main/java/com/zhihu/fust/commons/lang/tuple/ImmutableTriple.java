package com.zhihu.fust.commons.lang.tuple;

/**
 * <p>An immutable triple consisting of three {@code Object} elements.</p>
 *
 * <p>Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the triple, then the triple
 * itself effectively becomes mutable. The class is also {@code final}, so a subclass
 * can not add undesirable behaviour.</p>
 *
 * <p>#ThreadSafe# if all three objects are thread-safe</p>
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 * @since 3.2
 */
public final class ImmutableTriple<L, M, R> extends Triple<L, M, R> {

    /**
     * An immutable triple of nulls.
     */
    // This is not defined with generics to avoid warnings in call sites.
    @SuppressWarnings("rawtypes")
    private static final ImmutableTriple NULL = of(null, null, null);

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Returns an immutable triple of nulls.
     *
     * @param <L> the left element of this triple. Value is {@code null}.
     * @param <M> the middle element of this triple. Value is {@code null}.
     * @param <R> the right element of this triple. Value is {@code null}.
     * @return an immutable triple of nulls.
     * @since 3.6
     */
    @SuppressWarnings("unchecked")
    public static <L, M, R> ImmutableTriple<L, M, R> nullTriple() {
        return NULL;
    }

    /**
     * Left object
     */
    public final L left;
    /**
     * Middle object
     */
    public final M middle;
    /**
     * Right object
     */
    public final R right;

    /**
     * <p>Obtains an immutable triple of three objects inferring the generic types.</p>
     *
     * <p>This factory allows the triple to be created using inference to
     * obtain the generic types.</p>
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param left   the left element, may be null
     * @param middle the middle element, may be null
     * @param right  the right element, may be null
     * @return a triple formed from the three parameters, not null
     */
    public static <L, M, R> ImmutableTriple<L, M, R> of(final L left, final M middle, final R right) {
        return new ImmutableTriple<>(left, middle, right);
    }

    /**
     * Create a new triple instance.
     *
     * @param left   the left value, may be null
     * @param middle the middle value, may be null
     * @param right  the right value, may be null
     */
    public ImmutableTriple(final L left, final M middle, final R right) {
        super();
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    //-----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public L getLeft() {
        return left;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M getMiddle() {
        return middle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public R getRight() {
        return right;
    }
}

