package org.cloudx.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Statement {
  @JsonProperty("Action")
  private Object action;
  @JsonProperty("Resource")
  private String resource;
  @JsonProperty("Effect")
  private String effect;

  public Statement(Object action, String resource, String effect) {
    this.action = action;
    this.resource = resource;
    this.effect = effect;
  }

  public Statement() {}

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Statement that = (Statement) o;
    return Objects.equals(action, that.action) && Objects.equals(resource, that.resource)
        && Objects.equals(effect, that.effect);
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, resource, effect);
  }

  @Override
  public String toString() {
    return "PolicyDocumentStatement{" +
        "Action=" + action +
        ", Resource='" + resource + '\'' +
        ", Effect='" + effect + '\'' +
        '}';
  }
}
