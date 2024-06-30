package org.example.Model;

public class File
{
    private int id_file;
    private int id_user_upload;
    private String filename;
    private String filepath;
    private String filetype;

    public int getId_file()
    {
        return id_file;
    }

    public void setId_file(int id_file)
    {
        this.id_file = id_file;
    }

    public int getId_user_upload()
    {
        return id_user_upload;
    }

    public void setId_user_upload(int id_user_upload)
    {
        this.id_user_upload = id_user_upload;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public String getFilepath()
    {
        return filepath;
    }

    public void setFilepath(String filepath)
    {
        this.filepath = filepath;
    }

    public String getFiletype()
    {
        return filetype;
    }

    public void setFiletype(String filetype)
    {
        this.filetype = filetype;
    }


    public File(int id_file, int id_user_upload, String filename, String filepath, String filetype)
    {
        this.id_file = id_file;
        this.id_user_upload = id_user_upload;
        this.filename = filename;
        this.filepath = filepath;
        this.filetype = filetype;
    }
}
