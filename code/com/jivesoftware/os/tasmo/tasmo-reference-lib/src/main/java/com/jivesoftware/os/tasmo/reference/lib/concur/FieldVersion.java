/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.os.tasmo.reference.lib.concur;

import com.jivesoftware.os.jive.utils.id.ObjectId;
import java.util.Objects;

/**
 *
 * @author jonathan.colt
 */
public class FieldVersion {
    final ObjectId objectId;
    final String fieldName;
    final Long version;

    public FieldVersion(ObjectId objectId, String fieldName, Long version) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId cannot be null");
        }
        if (fieldName == null) {
            throw new IllegalArgumentException("fieldName cannot be null");
        }
        this.objectId = objectId;
        this.fieldName = fieldName;
        this.version = version;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        // Hacked for human readability when in a list.
        return objectId + "." + fieldName + "=" + version;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.objectId);
        hash = 37 * hash + Objects.hashCode(this.fieldName);
        hash = 37 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldVersion other = (FieldVersion) obj;
        if (!Objects.equals(this.objectId, other.objectId)) {
            return false;
        }
        if (!Objects.equals(this.fieldName, other.fieldName)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }

}
