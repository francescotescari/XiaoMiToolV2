package com.xiaomitool.v2.procedure;

import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.procedure.install.InstallException;
import com.xiaomitool.v2.utility.CommandClass;

public abstract class RNode extends RInstall {
  public static final int FLAG_THROWRAWEXCEPTION = 0x00000008;
  private static final int FLAG_THROWEXCEPTION = 0x00000001;
  private static final int FLAG_SORTRANDOM = 0x00000002;
  private static final int FLAG_SKIPONEXCEPTION = 0x00000004;
  protected RInstall[] children;
  protected NodeType type;
  protected boolean throwException = false;

  private RNode(NodeType type, RInstall... children) {
    this.type = type;
    this.children = children;
  }

  public static RInstall setSkipOnException(RInstall install) {
    install.setFlag(FLAG_SKIPONEXCEPTION, true);
    return install;
  }

  public static RInstall skipOnException(RInstall... installs) {
    for (RInstall install : installs) {
      setSkipOnException(install);
    }
    return RNode.sequence(installs);
  }

  public static RNode sequence(RInstall... chidren) {
    return new RNodeSequence(chidren);
  }

  public static RNode fallback(RInstall... chidren) {
    for (RInstall install : chidren) {
      install.setFlag(FLAG_THROWEXCEPTION, true);
    }
    return new RNodeFallback(chidren);
  }

  public static RInstall conditional(String keyBoolean, RInstall ifTrue, RInstall ifFalse) {
    return new RInstall() {
      @Override
      public void run(ProcedureRunner runner)
          throws InstallException, RMessage, InterruptedException {
        Boolean ifBody = (Boolean) runner.requireContext(keyBoolean);
        if (ifBody) {
          if (ifTrue != null) {
            ifTrue.run(runner);
          }
        } else {
          if (ifFalse != null) {
            ifFalse.run(runner);
          }
        }
      }
    };
  }

  public RInstall[] pushChildren(RInstall... children) {
    RInstall[] newChildren = new RInstall[this.children.length + children.length];
    int i = 0;
    for (RInstall child : this.children) {
      newChildren[i++] = child;
    }
    for (RInstall child : children) {
      newChildren[i++] = child;
    }
    this.children = newChildren;
    return newChildren;
  }

  @Override
  void runInternal(ProcedureRunner runner) throws InstallException, RMessage, InterruptedException {
    run(runner);
  }

  @Override
  public RInstall setFlag(int flag, boolean recursive) {
    super.setFlag(flag, recursive);
    if (recursive) {
      for (RInstall install : children) {
        install.setFlag(flag, true);
      }
    }
    return this;
  }

  public enum NodeType {
    SEQUENCE,
    FALLBACK,
    RANDOM
  }

  private static class RNodeSequence extends RNode {
    private RNodeSequence(RInstall... chidren) {
      super(NodeType.SEQUENCE, chidren);
    }

    @Override
    public void run(ProcedureRunner runner)
        throws RMessage, InstallException, InterruptedException {
      CommandClass.Command cmd = null;
      for (RInstall install : children) {
        do {
          try {
            install.runInternal(runner);
            break;
          } catch (InstallException e) {
            if (install.hasFlag(FLAG_SKIPONEXCEPTION)) {
              Log.warn(
                  "Exception occurred, but skip on exception flag was enabled: " + e.toString());
              break;
            } else if (install.hasFlag(FLAG_THROWEXCEPTION)) {
              throw new RMessage(e);
            } else {
              cmd = runner.handleException(e, install);
              if (CommandClass.Command.RETRY.equals(cmd)) {
                continue;
              } else if (CommandClass.Command.UPLEVEL.equals(cmd)) {
                break;
              }
              throw new RMessage(cmd);
            }
          } catch (RMessage rMessage) {
            if (rMessage.getCmd().equals(CommandClass.Command.RETRY)) {
              continue;
            } else if (rMessage.getCmd().equals(CommandClass.Command.UPLEVEL)) {
              cmd = CommandClass.Command.UPLEVEL;
              break;
            } else {
              throw rMessage;
            }
          }
        } while (true);
        if (CommandClass.Command.UPLEVEL.equals(cmd)) {
          this.runInternal(runner);
          return;
        }
      }
    }
  }

  private static class RNodeFallback extends RNode {
    private RNodeFallback(RInstall... chidren) {
      super(NodeType.FALLBACK, chidren);
    }

    @Override
    public void run(ProcedureRunner runner)
        throws InstallException, RMessage, InterruptedException {
      InstallException exception = null;
      boolean allRight = false;
      if (children == null || children.length == 0) {
        return;
      }
      RInstall cause = null;
      for (RInstall install : children) {
        exception = null;
        if (allRight) {
          break;
        }
        try {
          cause = install;
          install.runInternal(runner);
          allRight = true;
        } catch (InstallException e) {
          exception = e;
          Log.warn("Fallback: skipping install exception: " + e.getCode() + " - " + e.getMessage());
        } catch (RMessage e) {
          if (CommandClass.Command.EXCEPTION.equals(e.getCmd())) {
            exception = e.getInstallException();
          } else {
            throw e;
          }
        }
      }
      if (!allRight) {
        throw exception;
      }
    }
  }
}
