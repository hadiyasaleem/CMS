export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export type Database = {
  // Allows to automatically instantiate createClient with right options
  // instead of createClient<Database, { PostgrestVersion: 'XX' }>(URL, KEY)
  __InternalSupabase: {
    PostgrestVersion: "14.5"
  }
  public: {
    Tables: {
      academic_sessions: {
        Row: {
          created_at: string
          created_by: string | null
          current_semester: number
          deleted_at: string | null
          deleted_by: string | null
          dept_id: string
          end_year: number
          entity_id: number
          incharge_email: string | null
          is_active: boolean
          is_deleted: boolean
          max_students: number
          program_name: string | null
          session_id: string
          shift: Database["public"]["Enums"]["shift"]
          start_year: number
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          created_at?: string
          created_by?: string | null
          current_semester?: number
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id: string
          end_year: number
          entity_id?: never
          incharge_email?: string | null
          is_active?: boolean
          is_deleted?: boolean
          max_students?: number
          program_name?: string | null
          session_id: string
          shift: Database["public"]["Enums"]["shift"]
          start_year: number
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          created_at?: string
          created_by?: string | null
          current_semester?: number
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id?: string
          end_year?: number
          entity_id?: never
          incharge_email?: string | null
          is_active?: boolean
          is_deleted?: boolean
          max_students?: number
          program_name?: string | null
          session_id?: string
          shift?: Database["public"]["Enums"]["shift"]
          start_year?: number
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "academic_sessions_dept_id_fkey"
            columns: ["dept_id"]
            isOneToOne: false
            referencedRelation: "departments"
            referencedColumns: ["dept_id"]
          },
          {
            foreignKeyName: "academic_sessions_incharge_email_fkey"
            columns: ["incharge_email"]
            isOneToOne: false
            referencedRelation: "teachers"
            referencedColumns: ["email"]
          },
        ]
      }
      calendar_events: {
        Row: {
          audience: Database["public"]["Enums"]["notif_target"]
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          dept_id: string | null
          description: string | null
          end_date: string | null
          end_time: string | null
          entity_id: number
          event_type: Database["public"]["Enums"]["event_type"]
          id: string
          is_deleted: boolean
          session_id: string | null
          start_date: string
          start_time: string | null
          title: string
          updated_at: string
          updated_by: string | null
          venue: string | null
        }
        Insert: {
          audience?: Database["public"]["Enums"]["notif_target"]
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id?: string | null
          description?: string | null
          end_date?: string | null
          end_time?: string | null
          entity_id?: never
          event_type: Database["public"]["Enums"]["event_type"]
          id?: string
          is_deleted?: boolean
          session_id?: string | null
          start_date: string
          start_time?: string | null
          title: string
          updated_at?: string
          updated_by?: string | null
          venue?: string | null
        }
        Update: {
          audience?: Database["public"]["Enums"]["notif_target"]
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id?: string | null
          description?: string | null
          end_date?: string | null
          end_time?: string | null
          entity_id?: never
          event_type?: Database["public"]["Enums"]["event_type"]
          id?: string
          is_deleted?: boolean
          session_id?: string | null
          start_date?: string
          start_time?: string | null
          title?: string
          updated_at?: string
          updated_by?: string | null
          venue?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "calendar_events_dept_id_fkey"
            columns: ["dept_id"]
            isOneToOne: false
            referencedRelation: "departments"
            referencedColumns: ["dept_id"]
          },
          {
            foreignKeyName: "calendar_events_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "calendar_events_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      datesheet_slots: {
        Row: {
          building: string | null
          course_code: string | null
          created_at: string
          created_by: string | null
          datesheet_id: string
          deleted_at: string | null
          deleted_by: string | null
          duration_minutes: number | null
          end_time: string | null
          entity_id: number
          exam_date: string
          id: string
          invigilator_email: string | null
          is_deleted: boolean
          room_no: string | null
          start_time: string | null
          subject_name: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          building?: string | null
          course_code?: string | null
          created_at?: string
          created_by?: string | null
          datesheet_id: string
          deleted_at?: string | null
          deleted_by?: string | null
          duration_minutes?: number | null
          end_time?: string | null
          entity_id?: never
          exam_date: string
          id?: string
          invigilator_email?: string | null
          is_deleted?: boolean
          room_no?: string | null
          start_time?: string | null
          subject_name?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          building?: string | null
          course_code?: string | null
          created_at?: string
          created_by?: string | null
          datesheet_id?: string
          deleted_at?: string | null
          deleted_by?: string | null
          duration_minutes?: number | null
          end_time?: string | null
          entity_id?: never
          exam_date?: string
          id?: string
          invigilator_email?: string | null
          is_deleted?: boolean
          room_no?: string | null
          start_time?: string | null
          subject_name?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "datesheet_slots_datesheet_id_fkey"
            columns: ["datesheet_id"]
            isOneToOne: false
            referencedRelation: "datesheets"
            referencedColumns: ["id"]
          },
          {
            foreignKeyName: "datesheet_slots_invigilator_email_fkey"
            columns: ["invigilator_email"]
            isOneToOne: false
            referencedRelation: "teachers"
            referencedColumns: ["email"]
          },
        ]
      }
      datesheets: {
        Row: {
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          exam_type: Database["public"]["Enums"]["exam_type"] | null
          id: string
          instructions: string | null
          is_deleted: boolean
          published: boolean
          session_id: string | null
          title: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_type?: Database["public"]["Enums"]["exam_type"] | null
          id?: string
          instructions?: string | null
          is_deleted?: boolean
          published?: boolean
          session_id?: string | null
          title: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_type?: Database["public"]["Enums"]["exam_type"] | null
          id?: string
          instructions?: string | null
          is_deleted?: boolean
          published?: boolean
          session_id?: string | null
          title?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "datesheets_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "datesheets_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      departments: {
        Row: {
          code: string
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          dept_id: string
          description: string | null
          entity_id: number
          hod_email: string | null
          is_active: boolean
          is_deleted: boolean
          name: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          code: string
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id: string
          description?: string | null
          entity_id?: never
          hod_email?: string | null
          is_active?: boolean
          is_deleted?: boolean
          name: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          code?: string
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id?: string
          description?: string | null
          entity_id?: never
          hod_email?: string | null
          is_active?: boolean
          is_deleted?: boolean
          name?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "fk_departments_hod"
            columns: ["hod_email"]
            isOneToOne: false
            referencedRelation: "teachers"
            referencedColumns: ["email"]
          },
        ]
      }
      exam_paper_submissions: {
        Row: {
          course_code: string
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          exam_type: Database["public"]["Enums"]["exam_type"]
          file_name: string
          file_size_bytes: number | null
          id: string
          is_deleted: boolean
          key_storage_path: string | null
          mime_type: string | null
          review_status: Database["public"]["Enums"]["review_status"]
          reviewed_at: string | null
          reviewed_by: string | null
          semester: number
          session_id: string
          storage_path: string
          teacher_email: string
          teacher_notes: string | null
          updated_at: string
          updated_by: string | null
          uploaded_at: string
        }
        Insert: {
          course_code: string
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_type: Database["public"]["Enums"]["exam_type"]
          file_name: string
          file_size_bytes?: number | null
          id?: string
          is_deleted?: boolean
          key_storage_path?: string | null
          mime_type?: string | null
          review_status?: Database["public"]["Enums"]["review_status"]
          reviewed_at?: string | null
          reviewed_by?: string | null
          semester: number
          session_id: string
          storage_path: string
          teacher_email: string
          teacher_notes?: string | null
          updated_at?: string
          updated_by?: string | null
          uploaded_at?: string
        }
        Update: {
          course_code?: string
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_type?: Database["public"]["Enums"]["exam_type"]
          file_name?: string
          file_size_bytes?: number | null
          id?: string
          is_deleted?: boolean
          key_storage_path?: string | null
          mime_type?: string | null
          review_status?: Database["public"]["Enums"]["review_status"]
          reviewed_at?: string | null
          reviewed_by?: string | null
          semester?: number
          session_id?: string
          storage_path?: string
          teacher_email?: string
          teacher_notes?: string | null
          updated_at?: string
          updated_by?: string | null
          uploaded_at?: string
        }
        Relationships: [
          {
            foreignKeyName: "exam_paper_submissions_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "exam_paper_submissions_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      fee_overrides: {
        Row: {
          amount: number
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          is_deleted: boolean
          label: string
          reason: string
          roll_number: string
          session_id: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          amount: number
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          label: string
          reason?: string
          roll_number: string
          session_id: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          amount?: number
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          label?: string
          reason?: string
          roll_number?: string
          session_id?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "fee_overrides_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "fee_overrides_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
      fines: {
        Row: {
          amount: number
          category: Database["public"]["Enums"]["fine_category"]
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          id: string
          is_deleted: boolean
          issued_at: string
          issued_by: string | null
          reason: string
          roll_number: string
          session_id: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          amount: number
          category?: Database["public"]["Enums"]["fine_category"]
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          id?: string
          is_deleted?: boolean
          issued_at?: string
          issued_by?: string | null
          reason: string
          roll_number: string
          session_id: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          amount?: number
          category?: Database["public"]["Enums"]["fine_category"]
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          id?: string
          is_deleted?: boolean
          issued_at?: string
          issued_by?: string | null
          reason?: string
          roll_number?: string
          session_id?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "fines_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "fines_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
      mark_edit_requests: {
        Row: {
          course_code: string
          created_at: string
          created_by: string | null
          current_score: number | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          exam_type: Database["public"]["Enums"]["exam_type"]
          id: string
          is_deleted: boolean
          reason: string | null
          requested_at: string
          requested_by: string
          requested_score: number
          reviewed_at: string | null
          reviewed_by: string | null
          roll_number: string
          semester: number
          session_id: string
          status: Database["public"]["Enums"]["mark_edit_status"]
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          course_code: string
          created_at?: string
          created_by?: string | null
          current_score?: number | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_type: Database["public"]["Enums"]["exam_type"]
          id?: string
          is_deleted?: boolean
          reason?: string | null
          requested_at?: string
          requested_by: string
          requested_score: number
          reviewed_at?: string | null
          reviewed_by?: string | null
          roll_number: string
          semester: number
          session_id: string
          status?: Database["public"]["Enums"]["mark_edit_status"]
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          course_code?: string
          created_at?: string
          created_by?: string | null
          current_score?: number | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_type?: Database["public"]["Enums"]["exam_type"]
          id?: string
          is_deleted?: boolean
          reason?: string | null
          requested_at?: string
          requested_by?: string
          requested_score?: number
          reviewed_at?: string | null
          reviewed_by?: string | null
          roll_number?: string
          semester?: number
          session_id?: string
          status?: Database["public"]["Enums"]["mark_edit_status"]
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "mark_edit_requests_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "mark_edit_requests_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      notifications: {
        Row: {
          attachment_path: string | null
          body: string
          created_at: string
          created_by: string | null
          created_by_email: string
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          expires_at: string | null
          id: string
          is_deleted: boolean
          priority: Database["public"]["Enums"]["notif_priority"]
          target_dept_id: string | null
          target_role: Database["public"]["Enums"]["notif_target"] | null
          target_session_id: string | null
          title: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          attachment_path?: string | null
          body: string
          created_at?: string
          created_by?: string | null
          created_by_email: string
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          expires_at?: string | null
          id?: string
          is_deleted?: boolean
          priority?: Database["public"]["Enums"]["notif_priority"]
          target_dept_id?: string | null
          target_role?: Database["public"]["Enums"]["notif_target"] | null
          target_session_id?: string | null
          title: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          attachment_path?: string | null
          body?: string
          created_at?: string
          created_by?: string | null
          created_by_email?: string
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          expires_at?: string | null
          id?: string
          is_deleted?: boolean
          priority?: Database["public"]["Enums"]["notif_priority"]
          target_dept_id?: string | null
          target_role?: Database["public"]["Enums"]["notif_target"] | null
          target_session_id?: string | null
          title?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "notifications_target_dept_id_fkey"
            columns: ["target_dept_id"]
            isOneToOne: false
            referencedRelation: "departments"
            referencedColumns: ["dept_id"]
          },
          {
            foreignKeyName: "notifications_target_session_id_fkey"
            columns: ["target_session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "notifications_target_session_id_fkey"
            columns: ["target_session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      period_sessions: {
        Row: {
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          is_deleted: boolean
          period_id: string
          session_id: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          period_id: string
          session_id: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          period_id?: string
          session_id?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "period_sessions_period_id_fkey"
            columns: ["period_id"]
            isOneToOne: false
            referencedRelation: "timetable_periods"
            referencedColumns: ["id"]
          },
          {
            foreignKeyName: "period_sessions_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "period_sessions_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      profiles: {
        Row: {
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          email: string
          entity_id: number
          id: string
          is_deleted: boolean
          last_login_at: string | null
          linked_roll: string | null
          linked_session_id: string | null
          notification_prefs: Json
          role: Database["public"]["Enums"]["user_role"]
          status: Database["public"]["Enums"]["account_status"]
          teacher_email: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          email: string
          entity_id?: never
          id: string
          is_deleted?: boolean
          last_login_at?: string | null
          linked_roll?: string | null
          linked_session_id?: string | null
          notification_prefs?: Json
          role?: Database["public"]["Enums"]["user_role"]
          status?: Database["public"]["Enums"]["account_status"]
          teacher_email?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          email?: string
          entity_id?: never
          id?: string
          is_deleted?: boolean
          last_login_at?: string | null
          linked_roll?: string | null
          linked_session_id?: string | null
          notification_prefs?: Json
          role?: Database["public"]["Enums"]["user_role"]
          status?: Database["public"]["Enums"]["account_status"]
          teacher_email?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "fk_profiles_student"
            columns: ["linked_session_id", "linked_roll"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "fk_profiles_student"
            columns: ["linked_session_id", "linked_roll"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "profiles_teacher_email_fkey"
            columns: ["teacher_email"]
            isOneToOne: false
            referencedRelation: "teachers"
            referencedColumns: ["email"]
          },
        ]
      }
      semester_terms: {
        Row: {
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          end_date: string | null
          entity_id: number
          is_deleted: boolean
          semester: number
          session_id: string
          start_date: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          end_date?: string | null
          entity_id?: never
          is_deleted?: boolean
          semester: number
          session_id: string
          start_date?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          end_date?: string | null
          entity_id?: never
          is_deleted?: boolean
          semester?: number
          session_id?: string
          start_date?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "semester_terms_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "semester_terms_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      session_attendance: {
        Row: {
          course_code: string
          created_at: string
          created_by: string | null
          date: string
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          is_deleted: boolean
          is_late: boolean
          lecture_topic: string | null
          recorded_at: string
          remark: string | null
          roll_number: string
          semester: number
          session_id: string
          status: Database["public"]["Enums"]["attendance_status"]
          teacher_email: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          course_code: string
          created_at?: string
          created_by?: string | null
          date: string
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          is_late?: boolean
          lecture_topic?: string | null
          recorded_at?: string
          remark?: string | null
          roll_number: string
          semester: number
          session_id: string
          status: Database["public"]["Enums"]["attendance_status"]
          teacher_email?: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          course_code?: string
          created_at?: string
          created_by?: string | null
          date?: string
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          is_late?: boolean
          lecture_topic?: string | null
          recorded_at?: string
          remark?: string | null
          roll_number?: string
          semester?: number
          session_id?: string
          status?: Database["public"]["Enums"]["attendance_status"]
          teacher_email?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "session_attendance_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "session_attendance_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
      session_fee_heads: {
        Row: {
          amount: number
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          is_deleted: boolean
          label: string
          position: number
          session_id: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          amount: number
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          label: string
          position?: number
          session_id: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          amount?: number
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          label?: string
          position?: number
          session_id?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "session_fee_heads_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_fees"
            referencedColumns: ["session_id"]
          },
        ]
      }
      session_fees: {
        Row: {
          academic_year: string | null
          cadence: Database["public"]["Enums"]["fee_cadence"]
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          due_date: string | null
          entity_id: number
          is_deleted: boolean
          late_fine_note: string | null
          payment_note: string | null
          session_id: string
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          academic_year?: string | null
          cadence: Database["public"]["Enums"]["fee_cadence"]
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          due_date?: string | null
          entity_id?: never
          is_deleted?: boolean
          late_fine_note?: string | null
          payment_note?: string | null
          session_id: string
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          academic_year?: string | null
          cadence?: Database["public"]["Enums"]["fee_cadence"]
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          due_date?: string | null
          entity_id?: never
          is_deleted?: boolean
          late_fine_note?: string | null
          payment_note?: string | null
          session_id?: string
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "session_fees_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: true
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "session_fees_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: true
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      session_marks: {
        Row: {
          course_code: string
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          exam_date: string | null
          exam_type: Database["public"]["Enums"]["exam_type"]
          is_deleted: boolean
          max_marks: number
          remarks: string | null
          roll_number: string
          score: number | null
          semester: number
          session_id: string
          teacher_email: string
          updated_at: string
          updated_by: string | null
          was_absent: boolean
        }
        Insert: {
          course_code: string
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_date?: string | null
          exam_type: Database["public"]["Enums"]["exam_type"]
          is_deleted?: boolean
          max_marks: number
          remarks?: string | null
          roll_number: string
          score?: number | null
          semester: number
          session_id: string
          teacher_email?: string
          updated_at?: string
          updated_by?: string | null
          was_absent?: boolean
        }
        Update: {
          course_code?: string
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          exam_date?: string | null
          exam_type?: Database["public"]["Enums"]["exam_type"]
          is_deleted?: boolean
          max_marks?: number
          remarks?: string | null
          roll_number?: string
          score?: number | null
          semester?: number
          session_id?: string
          teacher_email?: string
          updated_at?: string
          updated_by?: string | null
          was_absent?: boolean
        }
        Relationships: [
          {
            foreignKeyName: "session_marks_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "session_marks_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
      session_students: {
        Row: {
          admission_date: string | null
          blood_group: string | null
          cgpa: number | null
          cnic_bform: string | null
          created_at: string
          created_by: string | null
          current_address: string | null
          deleted_at: string | null
          deleted_by: string | null
          dob: string | null
          domicile: string | null
          emergency_contact_name: string | null
          emergency_contact_phone: string | null
          emergency_contact_relation: string | null
          enrollment_status: Database["public"]["Enums"]["enrollment_status"]
          entity_id: number
          father_name: string | null
          gender: Database["public"]["Enums"]["gender"] | null
          gpa: number | null
          guardian_name: string | null
          guardian_phone: string | null
          is_active: boolean
          is_cr: boolean
          is_deleted: boolean
          is_gr: boolean
          linked_email: string
          name: string
          permanent_address: string | null
          personal_email: string | null
          phone: string | null
          photo_path: string | null
          registration_no: string | null
          religion: string | null
          roll_number: string
          session_id: string
          special_needs: string | null
          university_roll_no: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          admission_date?: string | null
          blood_group?: string | null
          cgpa?: number | null
          cnic_bform?: string | null
          created_at?: string
          created_by?: string | null
          current_address?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dob?: string | null
          domicile?: string | null
          emergency_contact_name?: string | null
          emergency_contact_phone?: string | null
          emergency_contact_relation?: string | null
          enrollment_status?: Database["public"]["Enums"]["enrollment_status"]
          entity_id?: never
          father_name?: string | null
          gender?: Database["public"]["Enums"]["gender"] | null
          gpa?: number | null
          guardian_name?: string | null
          guardian_phone?: string | null
          is_active?: boolean
          is_cr?: boolean
          is_deleted?: boolean
          is_gr?: boolean
          linked_email?: string
          name: string
          permanent_address?: string | null
          personal_email?: string | null
          phone?: string | null
          photo_path?: string | null
          registration_no?: string | null
          religion?: string | null
          roll_number: string
          session_id: string
          special_needs?: string | null
          university_roll_no?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          admission_date?: string | null
          blood_group?: string | null
          cgpa?: number | null
          cnic_bform?: string | null
          created_at?: string
          created_by?: string | null
          current_address?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dob?: string | null
          domicile?: string | null
          emergency_contact_name?: string | null
          emergency_contact_phone?: string | null
          emergency_contact_relation?: string | null
          enrollment_status?: Database["public"]["Enums"]["enrollment_status"]
          entity_id?: never
          father_name?: string | null
          gender?: Database["public"]["Enums"]["gender"] | null
          gpa?: number | null
          guardian_name?: string | null
          guardian_phone?: string | null
          is_active?: boolean
          is_cr?: boolean
          is_deleted?: boolean
          is_gr?: boolean
          linked_email?: string
          name?: string
          permanent_address?: string | null
          personal_email?: string | null
          phone?: string | null
          photo_path?: string | null
          registration_no?: string | null
          religion?: string | null
          roll_number?: string
          session_id?: string
          special_needs?: string | null
          university_roll_no?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "session_students_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "session_students_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      session_subjects: {
        Row: {
          course_code: string
          created_at: string
          created_by: string | null
          credit_hours: number
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          is_deleted: boolean
          is_elective: boolean
          name: string
          outline: string | null
          semester: number
          session_id: string
          subject_type: Database["public"]["Enums"]["subject_type"]
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          course_code: string
          created_at?: string
          created_by?: string | null
          credit_hours?: number
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          is_elective?: boolean
          name: string
          outline?: string | null
          semester: number
          session_id: string
          subject_type?: Database["public"]["Enums"]["subject_type"]
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          course_code?: string
          created_at?: string
          created_by?: string | null
          credit_hours?: number
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          is_deleted?: boolean
          is_elective?: boolean
          name?: string
          outline?: string | null
          semester?: number
          session_id?: string
          subject_type?: Database["public"]["Enums"]["subject_type"]
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "session_subjects_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "session_subjects_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      student_link_requests: {
        Row: {
          attempt_count: number
          cnic_bform_claimed: string | null
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          dob_claimed: string | null
          entity_id: number
          is_deleted: boolean
          message: string | null
          name_claimed: string | null
          registration_no_claimed: string | null
          rejection_reason: string | null
          request_id: string
          requested_by_email: string
          reviewed_at: string | null
          reviewed_by: string | null
          roll_number_claimed: string
          session_id: string | null
          status: Database["public"]["Enums"]["link_status"]
          university_roll_claimed: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          attempt_count?: number
          cnic_bform_claimed?: string | null
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dob_claimed?: string | null
          entity_id?: never
          is_deleted?: boolean
          message?: string | null
          name_claimed?: string | null
          registration_no_claimed?: string | null
          rejection_reason?: string | null
          request_id?: string
          requested_by_email: string
          reviewed_at?: string | null
          reviewed_by?: string | null
          roll_number_claimed: string
          session_id?: string | null
          status?: Database["public"]["Enums"]["link_status"]
          university_roll_claimed?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          attempt_count?: number
          cnic_bform_claimed?: string | null
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dob_claimed?: string | null
          entity_id?: never
          is_deleted?: boolean
          message?: string | null
          name_claimed?: string | null
          registration_no_claimed?: string | null
          rejection_reason?: string | null
          request_id?: string
          requested_by_email?: string
          reviewed_at?: string | null
          reviewed_by?: string | null
          roll_number_claimed?: string
          session_id?: string | null
          status?: Database["public"]["Enums"]["link_status"]
          university_roll_claimed?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "student_link_requests_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "student_link_requests_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      student_semester_gpa: {
        Row: {
          cgpa: number
          class_position: number | null
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          entity_id: number
          gpa: number
          is_deleted: boolean
          remarks: string | null
          result_status: Database["public"]["Enums"]["semester_result"]
          roll_number: string
          semester: number
          session_id: string
          supply_courses: string[]
          term_label: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          cgpa: number
          class_position?: number | null
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          gpa: number
          is_deleted?: boolean
          remarks?: string | null
          result_status?: Database["public"]["Enums"]["semester_result"]
          roll_number: string
          semester: number
          session_id: string
          supply_courses?: string[]
          term_label?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          cgpa?: number
          class_position?: number | null
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          entity_id?: never
          gpa?: number
          is_deleted?: boolean
          remarks?: string | null
          result_status?: Database["public"]["Enums"]["semester_result"]
          roll_number?: string
          semester?: number
          session_id?: string
          supply_courses?: string[]
          term_label?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "student_semester_gpa_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "student_semester_gpa_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
      teachers: {
        Row: {
          auth_uid: string | null
          can_approve_link_requests: boolean
          can_edit_timetable: boolean
          can_manage_datesheets: boolean
          can_send_notifications: boolean
          created_at: string
          created_by: string | null
          deleted_at: string | null
          deleted_by: string | null
          dept_id: string | null
          designation: string | null
          email: string
          entity_id: number
          gender: Database["public"]["Enums"]["gender"] | null
          is_active: boolean
          is_admin: boolean
          is_deleted: boolean
          is_hod: boolean
          name: string
          office_room: string | null
          phone: string | null
          photo_path: string | null
          qualification: string | null
          specialization: string | null
          status: Database["public"]["Enums"]["account_status"]
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          auth_uid?: string | null
          can_approve_link_requests?: boolean
          can_edit_timetable?: boolean
          can_manage_datesheets?: boolean
          can_send_notifications?: boolean
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id?: string | null
          designation?: string | null
          email: string
          entity_id?: never
          gender?: Database["public"]["Enums"]["gender"] | null
          is_active?: boolean
          is_admin?: boolean
          is_deleted?: boolean
          is_hod?: boolean
          name: string
          office_room?: string | null
          phone?: string | null
          photo_path?: string | null
          qualification?: string | null
          specialization?: string | null
          status?: Database["public"]["Enums"]["account_status"]
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          auth_uid?: string | null
          can_approve_link_requests?: boolean
          can_edit_timetable?: boolean
          can_manage_datesheets?: boolean
          can_send_notifications?: boolean
          created_at?: string
          created_by?: string | null
          deleted_at?: string | null
          deleted_by?: string | null
          dept_id?: string | null
          designation?: string | null
          email?: string
          entity_id?: never
          gender?: Database["public"]["Enums"]["gender"] | null
          is_active?: boolean
          is_admin?: boolean
          is_deleted?: boolean
          is_hod?: boolean
          name?: string
          office_room?: string | null
          phone?: string | null
          photo_path?: string | null
          qualification?: string | null
          specialization?: string | null
          status?: Database["public"]["Enums"]["account_status"]
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "teachers_dept_id_fkey"
            columns: ["dept_id"]
            isOneToOne: false
            referencedRelation: "departments"
            referencedColumns: ["dept_id"]
          },
        ]
      }
      timetable_periods: {
        Row: {
          building: string | null
          course_code: string | null
          created_at: string
          created_by: string | null
          credit_hours: number | null
          day: Database["public"]["Enums"]["weekday"]
          deleted_at: string | null
          deleted_by: string | null
          effective_from: string | null
          effective_to: string | null
          end_time: string
          entity_id: number
          id: string
          is_deleted: boolean
          notes: string | null
          period_type: Database["public"]["Enums"]["period_type"]
          primary_session_id: string
          room_no: string | null
          start_time: string
          subject_name: string | null
          teacher_email: string | null
          teacher_name: string | null
          updated_at: string
          updated_by: string | null
        }
        Insert: {
          building?: string | null
          course_code?: string | null
          created_at?: string
          created_by?: string | null
          credit_hours?: number | null
          day: Database["public"]["Enums"]["weekday"]
          deleted_at?: string | null
          deleted_by?: string | null
          effective_from?: string | null
          effective_to?: string | null
          end_time: string
          entity_id?: never
          id?: string
          is_deleted?: boolean
          notes?: string | null
          period_type?: Database["public"]["Enums"]["period_type"]
          primary_session_id: string
          room_no?: string | null
          start_time: string
          subject_name?: string | null
          teacher_email?: string | null
          teacher_name?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Update: {
          building?: string | null
          course_code?: string | null
          created_at?: string
          created_by?: string | null
          credit_hours?: number | null
          day?: Database["public"]["Enums"]["weekday"]
          deleted_at?: string | null
          deleted_by?: string | null
          effective_from?: string | null
          effective_to?: string | null
          end_time?: string
          entity_id?: never
          id?: string
          is_deleted?: boolean
          notes?: string | null
          period_type?: Database["public"]["Enums"]["period_type"]
          primary_session_id?: string
          room_no?: string | null
          start_time?: string
          subject_name?: string | null
          teacher_email?: string | null
          teacher_name?: string | null
          updated_at?: string
          updated_by?: string | null
        }
        Relationships: [
          {
            foreignKeyName: "timetable_periods_primary_session_id_fkey"
            columns: ["primary_session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "timetable_periods_primary_session_id_fkey"
            columns: ["primary_session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "timetable_periods_teacher_email_fkey"
            columns: ["teacher_email"]
            isOneToOne: false
            referencedRelation: "teachers"
            referencedColumns: ["email"]
          },
        ]
      }
    }
    Views: {
      at_risk_students: {
        Row: {
          attendance: number | null
          cgpa: number | null
          name: string | null
          roll_number: string | null
          session_id: string | null
        }
        Relationships: [
          {
            foreignKeyName: "session_students_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "academic_sessions"
            referencedColumns: ["session_id"]
          },
          {
            foreignKeyName: "session_students_session_id_fkey"
            columns: ["session_id"]
            isOneToOne: false
            referencedRelation: "session_overview"
            referencedColumns: ["session_id"]
          },
        ]
      }
      exam_stats: {
        Row: {
          avg_score: number | null
          course_code: string | null
          entered: number | null
          exam_type: Database["public"]["Enums"]["exam_type"] | null
          max_score: number | null
          min_score: number | null
          out_of: number | null
          pass_rate: number | null
          semester: number | null
          session_id: string | null
          stddev: number | null
        }
        Relationships: []
      }
      session_attendance_summary: {
        Row: {
          absent: number | null
          course_code: string | null
          leave: number | null
          percentage: number | null
          present: number | null
          roll_number: string | null
          semester: number | null
          session_id: string | null
          total_marked: number | null
        }
        Relationships: [
          {
            foreignKeyName: "session_attendance_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "session_attendance_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
      session_overview: {
        Row: {
          avg_attendance: number | null
          avg_cgpa: number | null
          current_semester: number | null
          dept_id: string | null
          session_id: string | null
          shift: Database["public"]["Enums"]["shift"] | null
          students: number | null
        }
        Relationships: [
          {
            foreignKeyName: "academic_sessions_dept_id_fkey"
            columns: ["dept_id"]
            isOneToOne: false
            referencedRelation: "departments"
            referencedColumns: ["dept_id"]
          },
        ]
      }
      student_gpa_progression: {
        Row: {
          cgpa: number | null
          class_position: number | null
          gpa: number | null
          gpa_delta: number | null
          result_status: Database["public"]["Enums"]["semester_result"] | null
          roll_number: string | null
          semester: number | null
          session_id: string | null
          term_label: string | null
        }
        Relationships: [
          {
            foreignKeyName: "student_semester_gpa_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "at_risk_students"
            referencedColumns: ["session_id", "roll_number"]
          },
          {
            foreignKeyName: "student_semester_gpa_session_id_roll_number_fkey"
            columns: ["session_id", "roll_number"]
            isOneToOne: false
            referencedRelation: "session_students"
            referencedColumns: ["session_id", "roll_number"]
          },
        ]
      }
    }
    Functions: {
      audit_actor: { Args: never; Returns: string }
      bootstrap_admin_email: { Args: never; Returns: string }
      current_email: { Args: never; Returns: string }
      is_active_teacher: { Args: never; Returns: boolean }
      is_admin: { Args: never; Returns: boolean }
      my_roll: { Args: never; Returns: string }
      my_session: { Args: never; Returns: string }
      record_semester_result: {
        Args: {
          p_cgpa: number
          p_class_position?: number
          p_gpa: number
          p_remarks?: string
          p_result?: Database["public"]["Enums"]["semester_result"]
          p_roll: string
          p_semester: number
          p_session: string
          p_supply?: string[]
          p_term_label?: string
        }
        Returns: undefined
      }
      teacher_can: { Args: { flag: string }; Returns: boolean }
      teaches: { Args: { p_session: string }; Returns: boolean }
    }
    Enums: {
      account_status: "ACTIVE" | "DISABLED" | "BANNED"
      attendance_status: "PRESENT" | "ABSENT" | "LEAVE"
      doc_kind: "PROSPECTUS" | "RULES" | "REPORT" | "OTHER"
      enrollment_status:
        | "ACTIVE"
        | "PROMOTED"
        | "REPEATED"
        | "WITHDRAWN"
        | "GRADUATED"
      event_type: "HOLIDAY" | "EVENT" | "EXAM" | "DEADLINE"
      exam_type: "MIDTERM" | "SESSIONAL"
      fee_cadence: "ANNUAL" | "SEMESTER"
      fine_category:
        | "LIBRARY"
        | "ATTENDANCE"
        | "EXAM"
        | "DISCIPLINARY"
        | "OTHER"
      gender: "MALE" | "FEMALE" | "OTHER"
      link_status: "PENDING" | "APPROVED" | "REJECTED"
      mark_edit_status: "PENDING" | "APPROVED" | "REJECTED"
      notif_priority: "NORMAL" | "IMPORTANT" | "URGENT"
      notif_target: "ADMIN" | "TEACHER" | "STUDENT" | "ALL"
      period_type: "LECTURE" | "ZERO" | "BREAK"
      review_status: "SUBMITTED" | "REVIEWED"
      semester_result: "PROMOTED" | "REPEATED" | "PROBATION" | "PENDING"
      shift: "MORNING" | "EVENING"
      subject_type: "THEORY" | "LAB"
      user_role: "ADMIN" | "TEACHER" | "STUDENT"
      weekday:
        | "MONDAY"
        | "TUESDAY"
        | "WEDNESDAY"
        | "THURSDAY"
        | "FRIDAY"
        | "SATURDAY"
    }
    CompositeTypes: {
      [_ in never]: never
    }
  }
}

type DatabaseWithoutInternals = Omit<Database, "__InternalSupabase">

type DefaultSchema = DatabaseWithoutInternals[Extract<keyof Database, "public">]

export type Tables<
  DefaultSchemaTableNameOrOptions extends
    | keyof (DefaultSchema["Tables"] & DefaultSchema["Views"])
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends (DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof (DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"] &
        DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Views"])
    : never) = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? (DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"] &
      DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Views"])[TableName] extends {
      Row: infer R
    }
    ? R
    : never
  : DefaultSchemaTableNameOrOptions extends keyof (DefaultSchema["Tables"] &
        DefaultSchema["Views"])
    ? (DefaultSchema["Tables"] &
        DefaultSchema["Views"])[DefaultSchemaTableNameOrOptions] extends {
        Row: infer R
      }
      ? R
      : never
    : never

export type TablesInsert<
  DefaultSchemaTableNameOrOptions extends
    | keyof DefaultSchema["Tables"]
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends (DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"]
    : never) = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Insert: infer I
    }
    ? I
    : never
  : DefaultSchemaTableNameOrOptions extends keyof DefaultSchema["Tables"]
    ? DefaultSchema["Tables"][DefaultSchemaTableNameOrOptions] extends {
        Insert: infer I
      }
      ? I
      : never
    : never

export type TablesUpdate<
  DefaultSchemaTableNameOrOptions extends
    | keyof DefaultSchema["Tables"]
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends (DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"]
    : never) = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Update: infer U
    }
    ? U
    : never
  : DefaultSchemaTableNameOrOptions extends keyof DefaultSchema["Tables"]
    ? DefaultSchema["Tables"][DefaultSchemaTableNameOrOptions] extends {
        Update: infer U
      }
      ? U
      : never
    : never

export type Enums<
  DefaultSchemaEnumNameOrOptions extends
    | keyof DefaultSchema["Enums"]
    | { schema: keyof DatabaseWithoutInternals },
  EnumName extends (DefaultSchemaEnumNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaEnumNameOrOptions["schema"]]["Enums"]
    : never) = never,
> = DefaultSchemaEnumNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaEnumNameOrOptions["schema"]]["Enums"][EnumName]
  : DefaultSchemaEnumNameOrOptions extends keyof DefaultSchema["Enums"]
    ? DefaultSchema["Enums"][DefaultSchemaEnumNameOrOptions]
    : never

export type CompositeTypes<
  PublicCompositeTypeNameOrOptions extends
    | keyof DefaultSchema["CompositeTypes"]
    | { schema: keyof DatabaseWithoutInternals },
  CompositeTypeName extends (PublicCompositeTypeNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"]
    : never) = never,
> = PublicCompositeTypeNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"][CompositeTypeName]
  : PublicCompositeTypeNameOrOptions extends keyof DefaultSchema["CompositeTypes"]
    ? DefaultSchema["CompositeTypes"][PublicCompositeTypeNameOrOptions]
    : never

export const Constants = {
  public: {
    Enums: {
      account_status: ["ACTIVE", "DISABLED", "BANNED"],
      attendance_status: ["PRESENT", "ABSENT", "LEAVE"],
      doc_kind: ["PROSPECTUS", "RULES", "REPORT", "OTHER"],
      enrollment_status: [
        "ACTIVE",
        "PROMOTED",
        "REPEATED",
        "WITHDRAWN",
        "GRADUATED",
      ],
      event_type: ["HOLIDAY", "EVENT", "EXAM", "DEADLINE"],
      exam_type: ["MIDTERM", "SESSIONAL"],
      fee_cadence: ["ANNUAL", "SEMESTER"],
      fine_category: ["LIBRARY", "ATTENDANCE", "EXAM", "DISCIPLINARY", "OTHER"],
      gender: ["MALE", "FEMALE", "OTHER"],
      link_status: ["PENDING", "APPROVED", "REJECTED"],
      mark_edit_status: ["PENDING", "APPROVED", "REJECTED"],
      notif_priority: ["NORMAL", "IMPORTANT", "URGENT"],
      notif_target: ["ADMIN", "TEACHER", "STUDENT", "ALL"],
      period_type: ["LECTURE", "ZERO", "BREAK"],
      review_status: ["SUBMITTED", "REVIEWED"],
      semester_result: ["PROMOTED", "REPEATED", "PROBATION", "PENDING"],
      shift: ["MORNING", "EVENING"],
      subject_type: ["THEORY", "LAB"],
      user_role: ["ADMIN", "TEACHER", "STUDENT"],
      weekday: [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
      ],
    },
  },
} as const
