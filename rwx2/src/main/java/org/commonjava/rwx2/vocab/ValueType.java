package org.commonjava.rwx2.vocab;

import org.apache.commons.codec.binary.Base64;
import org.commonjava.rwx2.error.CoercionException;
import org.commonjava.rwx2.util.ValueCoercion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.commonjava.rwx2.vocab.XmlRpcConstants.DATETIME_FORMAT;

public enum ValueType
{
    STRUCT( new ValueCoercion( "STRUCT non-coercion" )
    {
        @Override
        public String toString( final Object value ) throws CoercionException
        {
            throw new CoercionException( "Cannot coerce STRUCT types." );
        }

        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            throw new CoercionException( "Cannot coerce STRUCT types." );
        }
    }, Map.class, XmlRpcConstants.STRUCT ),

    ARRAY( new ValueCoercion( "ARRAY non-coercion" )
    {
        @Override
        public String toString( final Object value ) throws CoercionException
        {
            throw new CoercionException( "Cannot coerce ARRAY types." );
        }

        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            throw new CoercionException( "Cannot coerce ARRAY types." );
        }
    }, List.class, XmlRpcConstants.ARRAY ),

    NIL( new ValueCoercion( "NIL-to-null" )
    {
        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            return null;
        }

        @Override
        public String toString( final Object value )
        {
            return null;
        }
    }, Void.class, "nil" ),

    INT( new ValueCoercion( "INT-to-String" )
    {
        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            try
            {
                String val = value == null ? null : value.trim();
                return val == null || val.length() < 1 ? null : Integer.parseInt( val );
            }
            catch ( final NumberFormatException e )
            {
                throw new CoercionException( e.getMessage(), e );
            }
        }
    }, Integer.class, "int", "i4" ),

    BOOLEAN( new ValueCoercion( "BOOLEAN-to-String" )
    {
        @Override
        public String toString( final Object value ) throws CoercionException
        {
            if ( value == null )
            {
                return null;
            }
            else
            {
                final String s = super.toString( value ).trim();
                if ( "1".equals( s ) || "0".equals( s ) )
                {
                    return s;
                }
                else
                {
                    return Boolean.valueOf( s ) ? "1" : "0";
                }
            }
        }

        @Override
        public Object fromString( final String value )
        {
            if ( value == null )
            {
                return null;
            }
            else
            {
                String val = value.trim();
                return "1".equals( val ) || Boolean.valueOf( val );
            }
        }
    }, Boolean.class, "boolean" ),

    STRING( new ValueCoercion( "STRING-to-String (with trim)" )
    {
        @Override
        public Object fromString( final String value )
        {
            return value == null ? null : value.trim();
        }
    }, String.class, "string" ),

    DOUBLE( new ValueCoercion( "DOUBLE-to-String" )
    {
        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            try
            {
                String val = value == null ? null : value.trim();
                return val == null || val.length() < 1 ? null : Double.parseDouble( val );
            }
            catch ( final NumberFormatException e )
            {
                throw new CoercionException( e.getMessage(), e );
            }
        }

        @Override
        public String toString( final Object value )
        {
            return value == null ? null : Double.toString( ( (Number) value ).doubleValue() );
        }
    }, Number.class, "double" ),

    DATETIME( new ValueCoercion( "DATETIME-to-String (" + DATETIME_FORMAT + ")" )
    {
        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            try
            {
                String val = value == null ? null : value.trim();
                return val == null ? null : new SimpleDateFormat( DATETIME_FORMAT ).parse( value );
            }
            catch ( final ParseException e )
            {
                throw new CoercionException( "Cannot parse date: '" + value + "'.", e );
            }
        }

        @Override
        public String toString( final Object value ) throws CoercionException
        {
            try
            {
                return value == null ? null : new SimpleDateFormat( DATETIME_FORMAT ).format( (Date) value );
            }
            catch ( final ClassCastException e )
            {
                throw new CoercionException( "Not a java.util.Date.", e );
            }
        }

    }, Date.class, "dateTime.iso8601" ),

    BASE64( new ValueCoercion( "BASE64-to-String" )
    {
        @Override
        public Object fromString( final String value ) throws CoercionException
        {
            if ( value == null )
            {
                return null;
            }

            final byte[] result = Base64.decodeBase64( value.trim() );
            if ( result.length < 1 && value.length() > 0 && !value.equals( "==" ) && !value.equals( "=" ) )
            {
                throw new CoercionException( "Invalid Base64 input: " + value );
            }

            return result;
        }

        @Override
        public String toString( final Object value ) throws CoercionException
        {
            try
            {
                return value == null ?
                                null :
                                new String( Base64.encodeBase64( value instanceof String ?
                                                                                 ( (String) value ).getBytes() :
                                                                                 (byte[]) value ) );
            }
            catch ( final ClassCastException e )
            {
                throw new CoercionException( "Not a byte array.", e );
            }
        }

    }, byte[].class, "base64" ),;

    private String[] tags;

    private Class<?> nativeType;

    private ValueCoercion coercion;

    private ValueType( final ValueCoercion coercion, final Class<?> nativeType, final String... tags )
    {
        this.coercion = coercion;
        this.nativeType = nativeType;
        this.tags = tags;
    }

    public ValueCoercion coercion()
    {
        return coercion;
    }

    public String getPrimaryTag()
    {
        return tags[0];
    }

    public static ValueType safeTypeFor( final Object value )
    {
        final ValueType type = typeFor( value );
        return type == null ? STRING : type;
    }

    public static ValueType typeFor( final Object value )
    {
        ValueType result = null;

        if ( value == null )
        {
            result = NIL;
        }
        else
        {
            final Class<?> cls = value instanceof Class<?> ? (Class<?>) value : value.getClass();
            for ( final ValueType vt : values() )
            {
                if ( vt.nativeType.isAssignableFrom( cls ) )
                {
                    result = vt;
                    break;
                }
            }
        }

        return result;
    }

    public static ValueType typeOf( final String tag )
    {
        if ( tag == null || tag.trim().length() < 1 )
        {
            return STRING;
        }

        for ( final ValueType type : values() )
        {
            for ( final String t : type.tags )
            {
                if ( t.equals( tag ) )
                {
                    return type;
                }
            }
        }

        return STRING;
    }

    public String toString()
    {
        return name();
    }
}
