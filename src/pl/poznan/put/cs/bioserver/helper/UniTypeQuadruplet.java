package pl.poznan.put.cs.bioserver.helper;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public class UniTypeQuadruplet<T> {
    public final T a;
    public final T b;
    public final T c;
    public final T d;

    public UniTypeQuadruplet(List<T> list) {
        super();
        assert list.size() == 4;
        a = list.get(0);
        b = list.get(1);
        c = list.get(2);
        d = list.get(3);
    }

    public UniTypeQuadruplet(@Nullable T a, @Nullable T b, @Nullable T c,
            @Nullable T d) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public UniTypeQuadruplet(T[] array) {
        super();
        assert array.length == 4;
        a = array[0];
        b = array[1];
        c = array[2];
        d = array[3];
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UniTypeQuadruplet other = (UniTypeQuadruplet) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        if (c == null) {
            if (other.c != null) {
                return false;
            }
        } else if (!c.equals(other.c)) {
            return false;
        }
        if (d == null) {
            if (other.d != null) {
                return false;
            }
        } else if (!d.equals(other.d)) {
            return false;
        }
        return true;
    }

    @Nullable
    public T get(int index) {
        assert index >= 0 && index <= 3;

        switch (index) {
        case 0:
            return a;
        case 1:
            return b;
        case 2:
            return c;
        case 3:
            return d;
        default:
            break;
        }

        throw new RuntimeException(
                "UniTypeQuaduplet.get(index) was called with index < 0 or index > 3");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (a == null ? 0 : a.hashCode());
        result = prime * result + (b == null ? 0 : b.hashCode());
        result = prime * result + (c == null ? 0 : c.hashCode());
        result = prime * result + (d == null ? 0 : d.hashCode());
        return result;
    }
}
