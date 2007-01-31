To make Mesquite load the Chromaseq modules correctly, you have to include a classpaths.xml file in Mesquite_Folder.
A template is:

<?xml version="1.0"?>
<mesquite>
        <classpath>../../Chromaseq/jars/ToLBaseClasses.jar</classpath>
        <classpath>../../Chromaseq/Mesquite_Folder</classpath>
</mesquite>

where "../../Chromaseq/" is the relative path from the "Mesquite_Folder" in the main "Mesquite Project"
to the local "Chromaseq" project.