package cn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class test {
	private static CredentialsProvider cp = new UsernamePasswordCredentialsProvider("hanxuquan", "0ipvsft5");
	public static boolean svnup() throws IOException {
        String cmd = String.format("svn up %s", "D:/test/newmap/");
//        System.out.println(config.excelDirectoty);
        boolean isSuccess = true;
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(new String(s.getBytes(), "UTF-8"));
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
            isSuccess = false;
        }
        System.out.println("svn update success=" + isSuccess);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }
//	public static void main(String[] args) throws IOException {
//		svnup();
//	}
	public static void main(String[] args) throws IOException {
		gitCheckout(new File("D:/test/test/"),"1.5");
	}
	public static void exe(String ss) throws IOException{
		boolean isSuccess = true;
        Process process = Runtime.getRuntime().exec(ss);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(new String(s.getBytes(), "UTF-8"));
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
            isSuccess = false;
        }
        System.out.println("success=" + isSuccess);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	public static void gitCheckout(File repoDir, String version) {
	    File RepoGitDir = new File(repoDir.getAbsolutePath() + "/.git");
	    if (!RepoGitDir.exists()) {
	        System.out.println("Error! Not Exists : " + RepoGitDir.getAbsolutePath());
	    } else {
	        Repository repo = null;
	        try {
	            repo = new FileRepository(RepoGitDir.getAbsolutePath());
	            Git git = new Git(repo);
	            CheckoutCommand checkout = git.checkout();
	            checkout.setName(version);
	            checkout.call();
	            System.out.println("Checkout to " + version);

	            PullCommand pullCmd = git.pull().setCredentialsProvider(cp);
	            pullCmd.call();

	            System.out.println("Pulled from remote repository to local repository at " + repo.getDirectory());
	        } catch (Exception e) {
	        	System.out.println(e.getMessage() + " : " + RepoGitDir.getAbsolutePath());
	        } finally {
	            if (repo != null) {
	                repo.close();
	            }
	        }
	    }
	}
	public static void gitPull(File repoDir) {
	    File RepoGitDir = new File(repoDir.getAbsolutePath() + "/.git");
	    if (!RepoGitDir.exists()) {
	    	System.out.println("Error! Not Exists : " + RepoGitDir.getAbsolutePath());
	    } else {
	        Repository repo = null;
	        try {
	            repo = new FileRepository(RepoGitDir.getAbsolutePath());
	            Git git = new Git(repo);
	            
	            PullCommand pullCmd = git.pull().setCredentialsProvider(cp);
	            pullCmd.call();

	            System.out.println("Pulled from remote repository to local repository at " + repo.getDirectory());
	        } catch (Exception e) {
	        	System.out.println(e.getMessage() + " : " + RepoGitDir.getAbsolutePath());
	        } finally {
	            if (repo != null) {
	                repo.close();
	            }
	        }
	    }
	}
}
