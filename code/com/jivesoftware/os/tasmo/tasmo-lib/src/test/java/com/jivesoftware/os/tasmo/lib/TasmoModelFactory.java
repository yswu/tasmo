package com.jivesoftware.os.tasmo.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.os.jive.utils.id.ChainedVersion;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.tasmo.configuration.BindingGenerator;
import com.jivesoftware.os.tasmo.configuration.ViewModel;
import com.jivesoftware.os.tasmo.model.ViewBinding;
import com.jivesoftware.os.tasmo.model.Views;
import com.jivesoftware.os.tasmo.model.path.ModelPath;
import com.jivesoftware.os.tasmo.model.path.ModelPathStep;
import com.jivesoftware.os.tasmo.model.path.ModelPathStepType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author jonathan.colt
 */
public class TasmoModelFactory {

    static public Views modelToViews(String... simpleBindings) {
        return bindsAsViews(parseModelPathStrings(simpleBindings));
    }

    static public Views modelToViews(ArrayNode views) throws Exception {
        List<ViewBinding> viewBindingsList = new LinkedList<>();
        BindingGenerator bindingGenerator = new BindingGenerator();

        for (JsonNode view : views) {
            ViewModel viewConfiguration = ViewModel.builder((ObjectNode) view).build();
            viewBindingsList.add(bindingGenerator.generate(null, viewConfiguration));
        }
        return bindsAsViews(viewBindingsList);
    }

    static public Views bindsAsViews(List<ViewBinding> viewBindings) {
        return new Views(new TenantId("master"), new ChainedVersion("0", "1"), viewBindings);
    }

    static public List<ViewBinding> parseModelPathStrings(List<String> simpleBindings) {
        return parseModelPathStrings(simpleBindings.toArray(new String[simpleBindings.size()]));
    }

    static public List<ViewBinding> parseModelPathStrings(String... simpleBindings) {
        return parseModelPathStrings(false, simpleBindings);
    }

    static public List<ViewBinding> parseModelPathStrings(boolean idCentric, String... simpleBindings) {
        ArrayListMultimap<String, ModelPath> viewBindings = ArrayListMultimap.create();

        for (String simpleBinding : simpleBindings) {
            String[] class_pathId_modelPath = toStringArray(simpleBinding, "::");
            List<ModelPath> bindings = viewBindings.get(class_pathId_modelPath[0].trim());

            bindings.add(buildPath(class_pathId_modelPath[1].trim(), class_pathId_modelPath[2].trim()));
        }

        List<ViewBinding> viewBindingsList = Lists.newArrayList();
        for (Map.Entry<String, Collection<ModelPath>> entry : viewBindings.asMap().entrySet()) {
            viewBindingsList.add(new ViewBinding(entry.getKey(), new ArrayList<>(entry.getValue()), false, idCentric, false, null));
        }

        return viewBindingsList;
    }

    static public ModelPath buildPath(String id, String path) {
        String[] pathMembers = toStringArray(path, "|");
        ModelPath.Builder builder = ModelPath.builder(id);
        int i = 0;
        for (String pathMember : pathMembers) {
            builder.addPathMember(toModelPathMember(i, pathMember.trim()));
            i++;
        }
        return builder.build();
    }

    static private ModelPathStep toModelPathMember(int sortPrecedence, String pathMember) {

        try {
            String[] memberParts = toStringArray(pathMember, ".");
            if (pathMember.contains("." + ModelPathStepType.ref + ".")
                    || pathMember.contains("." + ModelPathStepType.refs + ".")
                    || pathMember.contains("." + ModelPathStepType.centric_ref + ".")
                    || pathMember.contains("." + ModelPathStepType.centric_refs + ".")) {
                // Example: Content.ref_originalAuthor.ref.User
                Set<String> originClassName = splitClassNames(memberParts[0].trim());
                String refFieldName = memberParts[1].trim();
                ModelPathStepType stepType = ModelPathStepType.valueOf(memberParts[2].trim());
                Set<String> destinationClassName = splitClassNames(memberParts[3].trim());

                return new ModelPathStep(sortPrecedence == 0, originClassName,
                        refFieldName, stepType, destinationClassName, null);

            } else if (pathMember.contains("." + ModelPathStepType.backRefs + ".")
                    || pathMember.contains("." + ModelPathStepType.count + ".")
                    || pathMember.contains("." + ModelPathStepType.latest_backRef + ".")
                    || pathMember.contains("." + ModelPathStepType.centric_backRefs + ".")
                    || pathMember.contains("." + ModelPathStepType.centric_count + ".")
                    || pathMember.contains("." + ModelPathStepType.centric_latest_backRef + ".")) {

                // Example: Content.backRefs.VersionedContent.ref_parent
                // Example: Content.count.VersionedContent.ref_parent
                // Example: Content.latest_backRef.VersionedContent.ref_parent
                Set<String> destinationClassName = splitClassNames(memberParts[0].trim());
                ModelPathStepType stepType = ModelPathStepType.valueOf(memberParts[1].trim());
                Set<String> originClassName = splitClassNames(memberParts[2].trim());
                String refFieldName = memberParts[3].trim();

                return new ModelPathStep(sortPrecedence == 0, originClassName,
                        refFieldName, stepType, destinationClassName, null);

            } else {

                // Example: User.firstName
                String[] valueFieldNames = toStringArray(memberParts[1], ",");
                for (int i = 0; i < valueFieldNames.length; i++) {
                    valueFieldNames[i] = valueFieldNames[i].trim();
                }
                Set<String> originClassName = splitClassNames(memberParts[0].trim());

                return new ModelPathStep(sortPrecedence == 0, originClassName,
                        null, ModelPathStepType.value, null, Arrays.asList(valueFieldNames));

            }
        } catch (Exception x) {
            throw new RuntimeException("fail to parse " + pathMember, x);
        }
    }

    static private Set<String> splitClassNames(String classNames) {
        if (classNames.startsWith("[")) {
            classNames = classNames.replace("[", "");
            classNames = classNames.replace("]", "");

            return Sets.newHashSet(classNames.split("\\^"));
        } else {
            return Sets.newHashSet(classNames);
        }
    }

    static private String[] toStringArray(String string, String delim) {
        if (string == null || delim == null) {
            return new String[0];
        }
        StringTokenizer tokenizer = new StringTokenizer(string, delim);
        int tokenCount = tokenizer.countTokens();

        String[] tokens = new String[tokenCount];
        for (int i = 0; i < tokenCount; i++) {
            tokens[i] = tokenizer.nextToken();
        }
        return tokens;
    }
}
