/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.domain;

import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTimeZone;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Thies
 */
@Entity
@Table(name = "USERS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends DomainObject<Integer, User> {
    private static final long serialVersionUID = 2546435367535412269L;
    private static final String DEFAULT_USER_COUNTRY = "CN";
    private static final Boolean DEFAULT_EMAIL_REMINDER_ENABLED = Boolean.TRUE;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    private Integer userId;

    @NotNull
    @Column(name = "USERNAME", length = 64)
    private String username;

    @NotNull
    @Column(name = "PASSWORD", nullable = false, length = 128)
    private String password;

    @Column(name = "FIRST_NAME", length = 64)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false, length = 64)
    private String lastName;

    @Column(name = "EMAIL", length = 128)
    private String email;

    @Column(name = "ACTIVE")
    @Type(type = "yes_no")
    private Boolean active = Boolean.TRUE;

    @Column(name = "SALT")
    private Integer salt;

    @Column(name = "COUNTRY")
    private String country = DEFAULT_USER_COUNTRY;

    @Column(name = "SEND_REMINDER")
    @Type(type = "yes_no")
    private Boolean sendReminder = DEFAULT_EMAIL_REMINDER_ENABLED;

    @Column(name = "LAST_LOGIN")
    private Date lastLoginTime;

    @Column(name = "LAST_PASSWORD_CHANGE")
    private Date lastPasswordChangeTime;

    @Transient
    private String updatedPassword;

    @ManyToMany(targetEntity = UserRole.class,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    @JoinTable(name = "USER_TO_USERROLE",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE"))
    private Set<UserRole> userRoles = new HashSet<>();

    @ManyToMany(targetEntity = UserDepartment.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "USER_TO_DEPARTMENT",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "DEPARTMENT_ID"))
    private Set<UserDepartment> userDepartments = new HashSet<>();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "user")
    private Set<ProjectAssignment> projectAssignments;

    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID")
    private UserDepartment legacyDepartment;

    @Transient
    private Set<ProjectAssignment> inactiveProjectAssignments;

    @Transient
    private boolean deletable;

    public User() {
    }

    public User(Integer userId) {
        this.userId = userId;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(Integer userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User addUserRole(UserRole role) {
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }
        userRoles.add(role);

        return this;
    }

    public User deleteUserRole(UserRole role) {
        if (userRoles != null) {
            userRoles.remove(role);
        }
        return this;
    }

    public Optional<DateTimeZone> getTimezone() {
        for (UserDepartment userDepartment : userDepartments) {
            if (userDepartment.getTimeZone() != null) {
                return Optional.of(userDepartment.getTimeZone());
            }
        }

        return Optional.absent();
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();

        if (!StringUtils.isBlank(lastName)) {
            fullName.append(lastName);

            if (!StringUtils.isBlank(firstName)) {
                fullName.append(", ");
            }
        }

        if (!StringUtils.isBlank(firstName)) {
            fullName.append(firstName);
        }

        return fullName.toString();
    }

    public void addUserDepartment(UserDepartment userDepartment) {
        getUserDepartments().add(userDepartment);
    }

    public void setUserDepartment(UserDepartment userDepartment) {
        getUserDepartments().clear();
        getUserDepartments().add(userDepartment);
    }

    public void clearLegacyDepartment() {
        this.legacyDepartment = null;
    }

    public Set<UserDepartment> getUserDepartments() {
        return userDepartments;
    }

    public UserDepartment getUserDepartment() {
        return (userDepartments.size() > 0) ? userDepartments.iterator().next() : null;
    }

    public void setUserDepartments(Set<UserDepartment> userDepartments) {
        this.userDepartments = userDepartments;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserRole> getUserRoles() {
        return this.userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public Boolean isSendReminderEnabled() {
        return sendReminder;
    }

    public void setSendReminder(Boolean sendReminder) {
        this.sendReminder = sendReminder;
    }

    /**
     * @return the projectAssignments
     */
    public Set<ProjectAssignment> getProjectAssignments() {
        return projectAssignments;
    }

    /**
     * @param projectAssignments the projectAssignments to set
     */
    public void setProjectAssignments(Set<ProjectAssignment> projectAssignments) {
        this.projectAssignments = projectAssignments;
    }

    /**
     * @return the inactiveProjectAssignments
     */
    public Set<ProjectAssignment> getInactiveProjectAssignments() {
        return inactiveProjectAssignments;
    }

    public void addProjectAssignment(ProjectAssignment projectAssignment) {
        if (projectAssignments == null) {
            projectAssignments = new HashSet<>();
        }

        projectAssignments.add(projectAssignment);
    }

    /**
     * @param inactiveProjectAssignments the inactiveProjectAssignments to set
     */
    public void setInactiveProjectAssignments(Set<ProjectAssignment> inactiveProjectAssignments) {
        this.inactiveProjectAssignments = inactiveProjectAssignments;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastPasswordChangeTime() {
        return lastPasswordChangeTime;
    }

    public void setLastPasswordChangeTime(Date lastPasswordChangeTime) {
        this.lastPasswordChangeTime = lastPasswordChangeTime;
    }

    @Override
    public Integer getPK() {
        return userId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("userId", getUserId())
                .append("username", getUsername())
                .append("lastName", getLastName())
                .append("firstName", getFirstName())
                .append("country", getCountry())
                .append("lastLogin", getLastLoginTime())
                .append("lastPasswordChange", getLastPasswordChangeTime())
                .toString();
    }

    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(User object) {
        return new CompareToBuilder()
                .append(this.getLastName(), object.getLastName())
                .append(this.getFirstName(), object.getFirstName())
                .append(this.getUserId(), object.getUserId())
                .append(this.getCountry(), object.getCountry())
//                .append(this.getLastLoginTime(), object.getLastLoginTime())
//                .append(this.getLastPasswordChangeTime(), object.getLastPasswordChangeTime())
                .toComparison();
    }

    /**
     * @return the deletable
     */
    public boolean isDeletable() {
        return deletable;
    }

    /**
     * @param deletable the deletable to set
     */
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    /**
     * @return the salt
     */
    public Integer getSalt() {
        return salt;
    }

    /**
     * @param salt the salt to set
     */
    public void setSalt(Integer salt) {
        this.salt = salt;
    }

    /**
     * @return the updatedPassword
     */
    public String getUpdatedPassword() {
        return updatedPassword;
    }

    /**
     * @param updatedPassword the updatedPassword to set
     */
    public void setUpdatedPassword(String updatedPassword) {
        this.updatedPassword = updatedPassword;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof User)) {
            return false;
        }

        User castOther = (User) other;
        return new EqualsBuilder()
                .append(username, castOther.username)
                .append(firstName, castOther.firstName)
                .append(lastName, castOther.lastName)
                .append(email, castOther.email)
                .append(active, castOther.active)
                .append(country, castOther.country)
                .append(sendReminder, castOther.sendReminder)
//                .append(lastLoginTime, castOther.lastLoginTime)
//                .append(lastPasswordChangeTime, castOther.lastPasswordChangeTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(username)
                .append(firstName).append(lastName)
                .append(email)
                .append(active)
                .append(country)
                .append(sendReminder)
//                .append(lastLoginTime)
//                .append(lastPasswordChangeTime)
                .toHashCode();
    }
}
