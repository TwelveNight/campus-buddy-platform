export interface Page<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
}
