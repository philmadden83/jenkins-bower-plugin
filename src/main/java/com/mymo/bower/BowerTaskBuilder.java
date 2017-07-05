package com.mymo.bower;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class BowerTaskBuilder extends Builder {
    private static final int SUCCESS            = 0;

    private final String task;

    private final boolean sudo;

    @DataBoundConstructor
    public BowerTaskBuilder(String task, boolean sudo) {
        this.task       = task;
        this.sudo       = sudo;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {

            int result = launcher.launch()
                    .cmdAsSingleString(buildCommand())
                    .stderr(listener.getLogger())
                    .stdout(listener.getLogger())
                    .pwd(build.getWorkspace())
                    .join();

            return result == SUCCESS;

        } catch (IOException | InterruptedException e) {
            listener.fatalError(e.getMessage());
        }

        return false;
    }

    private String buildCommand() {
        StringBuilder stringBuilder;

        if (isSudo()) {
            stringBuilder = new StringBuilder(String.format("sudo %s %s", getDescriptor().getBowerHome(), getTask()));
        } else {
            stringBuilder = new StringBuilder(String.format("%s %s", getDescriptor().getBowerHome(), getTask()));
        }

        return stringBuilder.toString();
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static void appendFlag(final StringBuilder builder, final String flag, final String... opts) {
        builder.append(String.format(" --%s", flag));
        if (opts != null && opts.length > 0) {
            builder.append(String.format(" %s", opts));
        }
    }

    public String getTask() {
        return task;
    }

    public boolean isSudo() {
        return sudo;
    }

    @Override
    public GruntTaskBuildStepDescriptor getDescriptor() {
        return (GruntTaskBuildStepDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class GruntTaskBuildStepDescriptor extends BuildStepDescriptor<Builder> {
        private String bowerHome;

        public GruntTaskBuildStepDescriptor() {
            load();
        }

        public String getBowerHome() {
            return bowerHome;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Invoke top-level Bower tasks";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            bowerHome = formData.getString("bowerHome");
            save();
            return super.configure(req,formData);
        }
    }
}

