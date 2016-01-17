package com.example.administrator.filemanager;

import android.content.Context;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {
    //工具类最好不要带有资源，因为要NEW很多，所以不能保证资源同步
    //以下变量是共有资源，还可以用synconised来同步
    static File parentFile = new File("/");
    static long size = 0;
    static int length = 0;

    //打开文件夹
    List openFile(File f) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        //确定打开目录的类型
        if (f.canRead()) {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (files.length != 0) {
                    map.put("0", null);
                    map.put("1", "目录:" + f.getAbsolutePath());
                    map.put("2", "当前目录内容:" + files.length);
                    list.add(map);
                    //遍历所打开目录的子文件
                    for (File ff : files) {
                        if (ff.canRead()) {
                            if (ff.isDirectory()) {
                                map = new HashMap<String, Object>();
                                map.put("0", R.mipmap.dir);
                                map.put("1", ff);
                                map.put("2", "文件夹内容:" + ff.listFiles().length);
                                list.add(map);
                            } else {
                                map = new HashMap<String, Object>();
                                map.put("0", R.mipmap.file);
                                map.put("1", ff);
                                map.put("2", "可执行文件");
                                list.add(map);
                            }
                        } else {
                            map = new HashMap<String, Object>();
                            map.put("0", R.mipmap.wrong);
                            map.put("1", ff);
                            map.put("2", "不可读文件");
                            list.add(map);
                        }
                    }
                    parentFile = f;
                }
            } else if (f.isFile()) {
                //执行所打开的文件？？？？？？？？？？？？？？？？？？？？？
                try {
                    Runtime rt = Runtime.getRuntime();
                    rt.exec(f.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return list;
    }

    //键盘返回事件
    List back() {
        File f = parentFile.getParentFile();
        List list;
        if (f != null) {
            list = openFile(f);
            parentFile = f;
        } else {
            list = openFile(parentFile);
        }
        return list;
    }

    //创建文件夹
    void createFile(Context context, File clickFile, String fileName) {
        File f = new File(clickFile.getAbsolutePath() + File.separator + fileName);
        if (f.exists()) {
            Toast.makeText(context, "文件夹已存在", Toast.LENGTH_SHORT).show();
        } else {
            if (f.mkdir()) {
                Toast.makeText(context, "文件夹创建成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "文件夹创建失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //删除父文件
    void deleteFile(Context context, File f) {
        if (f.isFile()) {
            f.delete();
        } else if (f.isDirectory()) {
            deleteFiles(f);
            f.delete();
        }
        //遍历父目录刷新列表
        MainActivity activity = (MainActivity) context;
        activity.loadAdapter(openFile(parentFile));
        if (!f.exists()) {
            Toast.makeText(context, "文件删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "文件删除失败", Toast.LENGTH_SHORT).show();
        }

    }

    //遍历删除子文件
    void deleteFiles(File f) {
        File[] files = f.listFiles();
        if (0 == files.length) {
            f.delete();
        } else {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    files[i].delete();
                } else if (files[i].isDirectory()) {
                    deleteFiles(files[i]);
                    files[i].delete();
                }
            }
        }
    }

    //重命名文件
    void renameFile(Context context, File f, String name) {
        File file = new File(parentFile.getAbsolutePath() + File.separator + name);
        if (f.renameTo(file)) {
            //遍历父目录刷新列表
            MainActivity activity = (MainActivity) context;
            activity.loadAdapter(openFile(parentFile));
            Toast.makeText(context, "重命名成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "重命名失败", Toast.LENGTH_SHORT).show();
        }
    }

    //复制父文件
    void copyFiles(Context context, File source) {
        //确定目标文件名
        int m = 0;
        File target = new File(source.getAbsolutePath() + "(" + m + ")");
        while (target.exists()) {
            ++m;
            target = new File(source.getAbsolutePath() + "(" + m + ")");
        }
        //开始复制啦
        if (source.isFile()) {
            if (0 == source.length()) {
                try {
                    target.createNewFile();
                } catch (Exception e) {
                    Toast.makeText(context, "复制父空文件失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                copyFile(context, source, target);
            }
        } else if (source.isDirectory()) {
            if (target.mkdirs()) {
                if (0 != source.length()) {
                    File[] files = source.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isFile()) {
                            // 复制文件
                            copyFile(context, files[i], new File(target.getAbsolutePath() + File.separator + files[i].getName()));
                        } else if (files[i].isDirectory()) {
                            //复制目录
                            File sourceDir = new File(source.getAbsolutePath() + File.separator + files[i].getName());
                            File targetDir = new File(target.getAbsolutePath() + File.separator + files[i].getName());
                            copyDirectory(context, sourceDir, targetDir);
                        }
                    }
                }
            } else {
                Toast.makeText(context, "复制父文件夹失败", Toast.LENGTH_SHORT).show();
            }
        }
        if (target.exists()) {
            //遍历父目录刷新列表
            MainActivity activity = (MainActivity) context;
            activity.loadAdapter(openFile(parentFile));
            Toast.makeText(context, "复制成功,新文件名为：" + target.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    //复制子文件夹
    void copyDirectory(Context context, File source, File target) {
        if (target.mkdirs()) {
            if (0 != source.length()) {
                File[] files = source.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        File sourceFile = files[i];
                        File targetFile = new File(target.getAbsolutePath() + File.separator + files[i].getName());
                        copyFile(context, sourceFile, targetFile);
                    } else if (files[i].isDirectory()) {
                        File sourceDir = new File(source.getAbsolutePath() + File.separator + files[i].getName());
                        File targetDir = new File(target.getAbsolutePath() + File.separator + files[i].getName());
                        copyDirectory(context, sourceDir, targetDir);
                    }
                }
            }
        } else {
            Toast.makeText(context, "复制子文件夹失败", Toast.LENGTH_SHORT).show();
        }


    }

    //复制子文件
    void copyFile(Context context, File source, File target) {
        if (0 == source.length()) {
            try {
                target.createNewFile();
            } catch (Exception e) {
                Toast.makeText(context, "复制子空文件失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            BufferedInputStream is = null;
            BufferedOutputStream os = null;
            try {
                is = new BufferedInputStream(new FileInputStream(source));
                os = new BufferedOutputStream(new FileOutputStream(target));
                byte[] buf = new byte[2048];
                int m;
                while ((m = is.read(buf)) != -1) {
                    os.write(buf, 0, m);
                }
                os.flush();
            } catch (Exception e) {
                Toast.makeText(context, "复制子文件失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    os.close();
                } catch (Exception e) {
                    Toast.makeText(context, "关闭复制管道失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    //移动文件
    void moveFile(Context context, File f, String path) {
        File file = new File(path + f.getName());
        if (f.renameTo(file)) {
            //遍历父目录刷新列表
            MainActivity activity = (MainActivity) context;
            activity.loadAdapter(openFile(parentFile));
            Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "移动失败", Toast.LENGTH_SHORT).show();
        }
    }

    //获取文件大小Runnable
    class SizeRunnable implements Runnable {
        File f;
        MyAdapter.SizeHandler sizeHandler;

        public SizeRunnable(File f, MyAdapter.SizeHandler sizeHandler) {
            this.f = f;
            this.sizeHandler = sizeHandler;
        }

        @Override
        public void run() {
            getSize(f, sizeHandler);
        }
    }

    //获取文件大小
    void getSize(File f, MyAdapter.SizeHandler sizeHandler) {
        if (f.isFile()) {
            size += f.length();
            //发送size的值让textView更新,必须用handler机制,用线程会报错
            Message msg = sizeHandler.obtainMessage();
            long b = size;
            long k = b / 1024;
            long m = b / 1024 / 1024;
            if (size >= 0 && b < 1024) {
                msg.obj = b + "B";
            } else if (k >= 1 && k < 1024) {
                msg.obj = k + "KB";
            } else if (m >= 1) {
                msg.obj = m + "MB";
            }
            sizeHandler.sendMessage(msg);
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (null != files) {
                for (File fs : files) {
                    getSize(fs, sizeHandler);
                }
            }
        }
    }

    //获取文件内容Runnable
    class LengthRunnable implements Runnable {
        File f;
        MyAdapter.LengthHandler lengthHandler;

        public LengthRunnable(File f, MyAdapter.LengthHandler lengthHandler) {
            this.f = f;
            this.lengthHandler = lengthHandler;
        }

        public void run() {
            getLength(f, lengthHandler);
        }
    }

    //获取文件内容
    void getLength(File f, MyAdapter.LengthHandler lengthHandler) {
        if (f.isFile()) {
            ++length;
            Message msg = lengthHandler.obtainMessage();
            msg.arg1 = length;
            lengthHandler.sendMessage(msg);
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (null != files) {
                for (File fs : files) {
                    getLength(fs, lengthHandler);
                }
            }
        }
    }

    //查找文件????????????Thread
    void findFile(File f) {
    }

}
