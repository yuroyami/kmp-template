# This app uploads no R8 mapping file, so release stack traces keep their real names.
# Delete `-dontobfuscate` when the app uploads a mapping file.
-dontobfuscate

# Keep the metadata that serializers and annotated SDK entry points read.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault
