/******************************************************************
 * File:        ValueStingArray.java
 * Created by:  Dave Reynolds
 * Created on:  29 Nov 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.dclib.values;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Wraps an array of strings, e.g. from a split operation. This allows
 * a pattern to return multiple results.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueArray extends ValueBase<Value[]> implements Value {
    
    public ValueArray(Value[] values, ConverterProcess proc) {
        super(values, proc);
    }
    
    public ValueArray(String[] values, ConverterProcess proc) {
        super(wrapStrings(values, proc), proc);
    }
    
    private static Value[] wrapStrings(String[] values, ConverterProcess proc) {
        Value[] wrapped = new Value[ values.length ];
        for (int i = 0; i < values.length; i++) {
            wrapped[i] = new ValueString(values[i], proc);
        }
        return wrapped;
    }

    @Override
    public boolean isNull() {
        return value == null || value.length == 0;
    }
    
    @Override
    public boolean isMulti() {
        return true;
    }

    @Override
    public Value[] getValues() {
        return value;
    }
    
    @Override
    public Value append(Value app) {
        if (app.isMulti()) {
            Value[] apps = app.getValues();
            int len = apps.length;
            Value[] results = new Value[value.length * len];
            for (int i = 0; i < value.length; i++) {
                for (int j = 0; j < len; j++) {
                    results[i*len + j] = value[i].append( apps[j] );
                }
            }
            return new ValueArray(results, proc);
        } else {
            String[] results = new String[value.length];
            for (int i = 0; i < value.length; i++) {
                results[i] = value[i] + app.toString();
            }
            return new ValueArray(results, proc);
        }
    }

    @Override
    public Value asString() {
        return this;
    }

    @Override
    public Node asNode() {
        return null;
    }

    @Override
    public String getDatatype() {
        return null;
    }
    
    public Value get(int i) {
        return value[i];
    }
    
    // Value methods applicable to any type
    
    public Object datatype(final String typeURI) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return new ValueNode( NodeFactory.createLiteral(value.toString(), typeFor(typeURI)), proc );        
            }
        });
    }
    
    protected RDFDatatype typeFor(String typeURI) {
        return TypeMapper.getInstance().getSafeTypeByName( expandTypeURI(typeURI) );
    }
    
    protected String expandTypeURI(String typeURI) {
        if (proc != null) {
            typeURI = proc.getDataContext().expandURI(typeURI);
        }
        if (typeURI.startsWith("xsd:")) {
            // Hardwired xsd: even if the prefix mapping doesn't have it
            typeURI = typeURI.replace("xsd:", XSD.getURI());
        }
        return typeURI;
    }

    public Object format(final String fmtstr) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return new ValueString(String.format(fmtstr, value), proc); 
            }
        });
        
    }

    public boolean isString() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isDate() {
        return false;
    }
    
    public Value asNumber() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                ValueNumber v = new ValueNumber(value.toString(), proc);
                if (v.isNull()) {
                    reportError("Could not convert " + value + " to a number");
                }
                return v;
            }
        });
    }    
    
    @Override
    public Value map(final String mapsource, final boolean matchRequired) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).map(mapsource, matchRequired);
            }
        });
    }
    
    public Value map(final String mapsource) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).map(mapsource);
            }
        });
    }
    
    public Value map(final String[] mapsources, final Object deflt) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).map(mapsources, deflt);
            }
        });
    }
    
    public Value asDate(final String format, final String typeURI) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return ValueDate.parse(this.toString(), format, expandTypeURI(typeURI), proc);
            }
        });        
    }
    
    public Value asDate(final String typeURI) {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return ValueDate.parse(this.toString(), expandTypeURI(typeURI), proc);
            }
        });
    }
    
    public Value toLowerCase() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return wrap(value.toString().toLowerCase());
            }
        });
    }
    
    public Value toUpperCase() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return wrap(value.toString().toUpperCase());
            }
        });
    }
    
    public Value toSegment() {
        return applyFunction(new MapValue() {
            public Value map(Value value) {
                return wrap( NameUtils.safeName(toString()) );
            }
        });
    }
    
    public Value toCleanSegment() {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).toCleanSegment();
            }
        });
    }
    
    public Value toSegment(final String repl) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).toSegment(repl);
            }
        });
    }
    
    public Value trim() {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).trim();
            }
        });
    }
    
    public Value substring(final int offset) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).substring(offset);
            }
        });
    }
    
    public Value substring(final int start, final int end) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).substring(start, end);
            }
        });
    }

    public Value replaceAll(final String regex, final String replacement) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).replaceAll(regex, replacement);
            }
        });
    }

    public Value regex(final String regex) {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).regex(regex);
            }
        });
    }
    
    public Value lastSegment() {
        return applyFunction(new MapValue() {
            @SuppressWarnings("rawtypes")
            public Value map(Value value) {
                return ((ValueBase)value).lastSegment();
            }
        });
    }
    
    public interface MapValue {
        public Value map(Value value);
    }
    
    public ValueArray applyFunction(MapValue map) {
        Value[] result = new Value[ value.length ];
        for (int i = 0; i < value.length; i++) {
            result[i] = map.map( value[i]);
        }
        return new ValueArray(result, proc);
    }
}

