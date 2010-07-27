/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonjava.rwx.binding.testutil;

import static org.commonjava.rwx.binding.testutil.recipe.RecipeEventUtils.endParameter;

import org.commonjava.rwx.binding.anno.BindVia;
import org.commonjava.rwx.binding.anno.DataIndex;
import org.commonjava.rwx.binding.anno.Request;
import org.commonjava.rwx.binding.anno.UnbindVia;
import org.commonjava.rwx.binding.convert.ListOfStringsConverter;
import org.commonjava.rwx.binding.mapping.ArrayMapping;
import org.commonjava.rwx.binding.mapping.FieldBinding;
import org.commonjava.rwx.binding.mapping.Mapping;
import org.commonjava.rwx.impl.estream.model.ArrayEvent;
import org.commonjava.rwx.impl.estream.model.Event;
import org.commonjava.rwx.impl.estream.model.ParameterEvent;
import org.commonjava.rwx.impl.estream.model.RequestEvent;
import org.commonjava.rwx.impl.estream.model.ValueEvent;
import org.commonjava.rwx.impl.estream.testutil.ExtList;
import org.commonjava.rwx.vocab.EventType;
import org.commonjava.rwx.vocab.ValueType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Request( method = "getPerson" )
public class SimpleConverterRequest
    implements TestObject
{

    @DataIndex( 0 )
    @BindVia( ListOfStringsConverter.class )
    @UnbindVia( ListOfStringsConverter.class )
    private List<String> userIds = Arrays.asList( new String[] { "foo", "bar" } );

    public List<String> getUserIds()
    {
        return userIds;
    }

    public void setUserIds( final List<String> userIds )
    {
        this.userIds = userIds;
    }

    public Map<Class<?>, Mapping<?>> recipes()
    {
        final Map<Class<?>, Mapping<?>> recipes = new HashMap<Class<?>, Mapping<?>>();

        final ArrayMapping recipe = new ArrayMapping( SimpleConverterRequest.class, new Integer[0] );

        final FieldBinding binding =
            new FieldBinding( "userIds", List.class ).withValueBinderType( ListOfStringsConverter.class )
                                                     .withValueUnbinderType( ListOfStringsConverter.class );

        recipe.addFieldBinding( 0, binding );
        recipes.put( SimpleConverterRequest.class, recipe );

        return recipes;
    }

    public List<Event<?>> events()
    {
        final ExtList<Event<?>> check = new ExtList<Event<?>>();

        check.withAll( new RequestEvent( true ), new RequestEvent( "getPerson" ) );

        check.with( new ParameterEvent( 0 ) );
        check.with( new ArrayEvent( EventType.START_ARRAY ) );

        for ( int i = 0; i < userIds.size(); i++ )
        {
            check.with( new ArrayEvent( i ) );
            check.with( new ValueEvent( userIds.get( i ), ValueType.STRING ) );
            check.with( new ArrayEvent( i, userIds.get( i ), ValueType.STRING ) );
            check.with( new ArrayEvent( EventType.END_ARRAY_ELEMENT ) );
        }

        check.with( new ArrayEvent( EventType.END_ARRAY ) );
        check.withAll( endParameter( 0, userIds, ValueType.ARRAY ) );
        check.with( new RequestEvent( false ) );

        return check;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( userIds == null ) ? 0 : userIds.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final SimpleConverterRequest other = (SimpleConverterRequest) obj;
        if ( userIds == null )
        {
            if ( other.userIds != null )
            {
                return false;
            }
        }
        else if ( !userIds.equals( other.userIds ) )
        {
            return false;
        }
        return true;
    }

}