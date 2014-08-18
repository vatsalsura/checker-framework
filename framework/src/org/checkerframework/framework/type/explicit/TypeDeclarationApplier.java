package org.checkerframework.framework.type.explicit;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TargetType;

import static org.checkerframework.framework.type.explicit.ElementAnnotationUtil.applyAllElementAnnotations;
import static com.sun.tools.javac.code.TargetType.*;

import javax.lang.model.element.Element;
import java.util.List;

/**
 * Apply annotations to a declared type based on it's declaration.
 */
public class TypeDeclarationApplier extends TargetedElementAnnotationApplier {

    public static void apply(final AnnotatedTypeMirror type, final Element element, final AnnotatedTypeFactory typeFactory) {
        new TypeDeclarationApplier(type, element, typeFactory).extractAndApply();
    }

    /**
     * If a type_index == -1 it means that the index refers to the immediate supertype class
     * of the declaration.  There is only ever one of these since java has no multiple inheritance
     */
    public static int SUPERCLASS_INDEX = -1;

    /**
     * @return True if type is an annotated declared type and element is a ClassSymbol
     */
    public static boolean accepts(final AnnotatedTypeMirror type, final Element element) {
        return type instanceof AnnotatedDeclaredType &&
               element instanceof Symbol.ClassSymbol;
    }

    private final AnnotatedTypeFactory typeFactory;
    private final Symbol.ClassSymbol typeSymbol;
    private final AnnotatedDeclaredType declaredType;

    public TypeDeclarationApplier(final AnnotatedTypeMirror type, final Element element, final AnnotatedTypeFactory typeFactory) {
        super(type, element);
        this.typeFactory = typeFactory;
        this.typeSymbol = (Symbol.ClassSymbol) element;
        this.declaredType = (AnnotatedDeclaredType) type;
    }

    /**
     * @inherit
     */
    @Override
    protected TargetType[] validTargets() {
        return new TargetType[]{
            RESOURCE_VARIABLE, EXCEPTION_PARAMETER, NEW, CAST, INSTANCEOF, METHOD_INVOCATION_TYPE_ARGUMENT,
            CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT, METHOD_REFERENCE, CONSTRUCTOR_REFERENCE,
            METHOD_REFERENCE_TYPE_ARGUMENT, CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT,
            CLASS_TYPE_PARAMETER, CLASS_TYPE_PARAMETER_BOUND
        };
    }

    /**
     * @inherit
     */
    @Override
    protected TargetType[] annotatedTargets() {
        return new TargetType[]{ CLASS_EXTENDS };
    }

    /**
     * All TypeCompounds (annotations) on the ClassSymbol
     * @return
     */
    @Override
    protected Iterable<Attribute.TypeCompound> getRawTypeAttributes() {
        return typeSymbol.getRawTypeAttributes();
    }

    /**
     * While more than just annotations on extends or implements clause are annotated by this
     * class, only these annotations are passed to handleTargeted (as they are the only in the annotatedTargets
     * list).  See extractAndApply for type parameters
     * @param extendsAndImplementsAnnos Annotations with a TargetType of CLASS_EXTENDS
     */
    @Override
    protected void handleTargeted(List<Attribute.TypeCompound> extendsAndImplementsAnnos ) {
        for( final Attribute.TypeCompound anno : extendsAndImplementsAnnos) {

            if( anno.position.type_index >= SUPERCLASS_INDEX && anno.position.location.isEmpty()) {
                type.addAnnotation(anno);
            }
        }
    }

    /**
     *  Adds extends/implements and class annotations to type.  Annotates type parameters.
     */
    @Override
    public void extractAndApply() {
        //ensures that we check that there only valid target types on this class, there are no "targeted" locations
        super.extractAndApply();

        // Annotate raw types //TODO: ASK WERNER WHAT THIS MIGHT MEAN?  WHAT ACTUALLY GOES HERE?
        type.addAnnotations(typeSymbol.getAnnotationMirrors());

        applyAllElementAnnotations(declaredType.getTypeArguments(), typeSymbol.getTypeParameters(), typeFactory);
    }

    @Override
    protected boolean isAccepted() {
        return accepts(type, element);
    }
}
